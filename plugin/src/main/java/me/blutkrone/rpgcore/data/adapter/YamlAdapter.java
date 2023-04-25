package me.blutkrone.rpgcore.data.adapter;

import me.blutkrone.rpgcore.api.data.IDataAdapter;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class YamlAdapter implements IDataAdapter {

    private boolean working = false;

    /**
     * This is an unsafe method which can compromise data integrity, do not
     * use this under any circumstances unless you want to gamble with the
     * integrity of player data.
     *
     * @param config File to read from
     * @return Data bundles from file
     */
    public static Map<String, DataBundle> internalReadForMigration(ConfigWrapper config) {
        return internalRead(config);
    }

    private static void internalWrite(ConfigWrapper config, Map<String, DataBundle> bundles) {
        config.set("version", 1);
        bundles.forEach((path, bundle) -> {
            config.set(path, bundle.getHandle());
        });
    }

    private static Map<String, DataBundle> internalRead(ConfigWrapper config) {
        Map<String, DataBundle> bundles = new HashMap<>();

        int version = config.getInt("version", 0);
        if (version == 0) {
            // legacy storage protocol
            config.forEachWithSelf((path, root) -> {
                if (path.equals("version")) {
                    return;
                }

                DataBundle bundle = new DataBundle();
                int counter = 0;
                while (root.isSet(path + "." + counter)) {
                    ConfigWrapper raw = root.getSection(path + "." + counter);
                    String type = raw.getString("type");

                    if ("number".equalsIgnoreCase(type)) {
                        bundle.addNumber(raw.getDouble("value"));
                    } else if ("boolean".equalsIgnoreCase(type)) {
                        bundle.addBoolean(raw.getBoolean("value"));
                    } else if ("string".equalsIgnoreCase(type)) {
                        bundle.addString(raw.getString("value"));
                    } else if ("location".equalsIgnoreCase(type)) {
                        bundle.addLocation(raw.getLocation("value"));
                    }

                    counter += 1;
                }
                bundles.put(path, bundle);
            });
        } else if (version == 1) {
            // string driven protocol
            config.forEachWithSelf((path, root) -> {
                List<String> data = root.getStringList(path);
                DataBundle bundle = new DataBundle();
                bundle.getHandle().addAll(data);
                bundles.put(path, bundle);
            });
        }

        return bundles;
    }

    @Override
    public boolean isWorking() {
        return this.working;
    }

    @Override
    public synchronized void operateCustom(UUID uuid, String keyword, Consumer<Map<String, DataBundle>> process) {
        try {
            this.working = true;

            Map<String, DataBundle> loaded = loadCustom(uuid, keyword);
            process.accept(loaded);
            saveCustom(uuid, keyword, loaded);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.working = false;
        }
    }

    @Override
    public synchronized Map<String, DataBundle> loadCustom(UUID uuid, String keyword) throws IOException {
        try {
            this.working = true;

            File file = FileUtil.file("database", "custom-" + uuid + "-" + keyword + ".yml");
            ConfigWrapper config = FileUtil.asConfigYML(file);
            return internalRead(config);
        } finally {
            this.working = false;
        }
    }

    @Override
    public synchronized Map<String, DataBundle> loadRosterData(UUID uuid) throws IOException {
        try {
            this.working = true;

            File file = FileUtil.file("database", "roster-" + uuid + ".yml");
            ConfigWrapper config = FileUtil.asConfigYML(file);
            return internalRead(config);
        } finally {
            this.working = false;
        }
    }

    @Override
    public synchronized Map<String, DataBundle> loadCharacterData(UUID uuid, int character) throws IOException {
        try {
            this.working = true;

            File file = FileUtil.file("database", "character-" + uuid + "-" + character + ".yml");
            ConfigWrapper config = FileUtil.asConfigYML(file);
            return internalRead(config);
        } finally {
            this.working = false;
        }
    }

    @Override
    public synchronized void saveCustom(UUID uuid, String keyword, Map<String, DataBundle> data) throws IOException {
        try {
            this.working = true;

            File file = FileUtil.file("database", "custom-" + uuid + "-" + keyword + ".yml");
            ConfigWrapper config = FileUtil.asConfigYML(file);
            internalWrite(config, data);
            ((YamlConfiguration) config.getHandle()).save(file);
        } finally {
            this.working = false;
        }
    }

    @Override
    public synchronized void saveRosterData(UUID uuid, Map<String, DataBundle> data) throws IOException {
        try {
            this.working = true;

            File file = FileUtil.file("database", "roster-" + uuid + ".yml");
            ConfigWrapper config = FileUtil.asConfigYML(file);
            internalWrite(config, data);
            ((YamlConfiguration) config.getHandle()).save(file);
        } finally {
            this.working = false;
        }
    }

    @Override
    public synchronized void saveCharacterData(UUID uuid, int character, Map<String, DataBundle> data) throws IOException {
        try {
            this.working = true;

            File file = FileUtil.file("database", "character-" + uuid + "-" + character + ".yml");
            ConfigWrapper config = FileUtil.asConfigYML(file);
            internalWrite(config, data);
            ((YamlConfiguration) config.getHandle()).save(file);
        } finally {
            this.working = false;
        }
    }
}
