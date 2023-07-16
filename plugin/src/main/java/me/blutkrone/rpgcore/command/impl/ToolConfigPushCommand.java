package me.blutkrone.rpgcore.command.impl;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class ToolConfigPushCommand extends AbstractCommand {

    private static File RAW_FILE = new File(RPGCore.inst().getDataFolder().getParentFile(), "rpgcore-temp.zip");

    public static void pullConfig() {
        try {
            String token = Utility.getSyncDatabaseToken();
            if (token.startsWith("mongodb")) {
                // build database structure
                ConnectionString connectionString = new ConnectionString(token);
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(connectionString).build();
                MongoClient mongoClient = MongoClients.create(settings);
                MongoDatabase database = mongoClient.getDatabase("rpgcore-config");
                // cancel if version hasn't changed
                MongoCollection<Document> collection = database.getCollection("metadata");
                Document metadata_document = collection.find(new Document("type", "version")).first();
                if (metadata_document == null) {
                    return;
                }
                ConfigWrapper version_config = FileUtil.asConfigYML(FileUtil.file("network.yml"));
                String version_have = version_config.getString("current-config-version", "");
                String version_want = metadata_document.getString("version");
                if (version_want == null || version_want.isBlank()) {
                    return;
                }
                if (version_want.equals(version_have)) {
                    return;
                }

                RPGCore.inst().getLogger().info("Configuration synced from " + version_have + " to " + version_want);

                // wipe configuration and rebuild from database
                FileUtils.deleteDirectory(RPGCore.inst().getDataFolder());
                // pull each file individually
                collection = database.getCollection("files");
                for (Document document : collection.find()) {
                    String raw_path = document.getString("path");
                    String raw_data = document.getString("bytes");
                    // ensure parent directory exists
                    File file = new File(raw_path);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    // dump into target file
                    try {
                        byte[] bytes = Base64.getDecoder().decode(raw_data);
                        FileUtils.writeByteArrayToFile(file, bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // inform about synchronization completion
                RPGCore.inst().getLogger().info("Synchronized with pushed configurations, total " + collection.countDocuments() + " files!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return new BaseComponent[0];
    }

    public void pushConfig() {
        UUID version = UUID.randomUUID();

        try {
            // connect to the database
            String token = Utility.getSyncDatabaseToken();
            if (token.startsWith("mongodb")) {
                // build database structure
                ConnectionString connectionString = new ConnectionString(token);
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(connectionString).build();
                MongoClient mongoClient = MongoClients.create(settings);
                MongoDatabase database = mongoClient.getDatabase("rpgcore-config");
                // purge the last iteration of data
                MongoCollection<Document> collection = database.getCollection("files");
                collection.deleteMany(new Document());
                // update the version before pushing to the database
                try {
                    File version_file = FileUtil.file("network.yml");
                    ConfigWrapper version_config = FileUtil.asConfigYML(version_file);
                    version_config.set("current-config-version", version.toString());
                    FileUtil.saveToDirectory((YamlConfiguration) version_config.getHandle(), version_file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // keep track of the files that we now have
                List<File> files = Arrays.asList(FileUtil.buildAllFiles(RPGCore.inst().getDataFolder()));
                Iterator<File> iterator = files.iterator();
                new BukkitRunnable() {

                    int counter = 0;
                    int size = files.size();

                    @Override
                    public void run() {
                        long start = System.currentTimeMillis();
                        while (iterator.hasNext() && (System.currentTimeMillis() - start) < 50) {
                            File file = iterator.next();

                            try {
                                byte[] bytes = Files.readAllBytes(file.toPath());
                                String value = Base64.getEncoder().encodeToString(bytes);

                                Document file_document = new Document();
                                file_document.put("path", file.getPath());
                                file_document.put("bytes", value);
                                collection.insertOne(file_document);

                                counter += 1;
                            } catch (Exception e) {
                                RPGCore.inst().getLogger().severe("File " + file.getPath() + " is too large, and could not be pushed!");
                            }
                        }

                        RPGCore.inst().getLogger().info("Configurations synced to database: " + counter + "/" + size);

                        if (!iterator.hasNext()) {
                            // meta information
                            MongoCollection<Document> collection = database.getCollection("metadata");
                            Document document = new Document();
                            document.put("type", "version");
                            document.put("version", version.toString());
                            collection.replaceOne(new Document("type", "version"), document);
                            // inform about the change performed
                            RPGCore.inst().getLogger().info("Configuration was pushed, restart other servers to pull!");
                            // terminate the pushing task
                            cancel();
                        }
                    }
                }.runTaskTimer(RPGCore.inst(), 1, 1);
            } else {
                RPGCore.inst().getLogger().severe("Sync-Token '" + token + " ' in network.yml must be a MongoDB token!");
            }
        } catch (Exception e) {
            RPGCore.inst().getLogger().severe("Something went wrong while pushing!");
        }
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        RAW_FILE.delete();
        pushConfig();
        RAW_FILE.delete();
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}