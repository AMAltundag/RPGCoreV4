package me.blutkrone.rpgcore.data.adapter;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.blutkrone.rpgcore.api.data.IDataAdapter;
import me.blutkrone.rpgcore.data.DataBundle;
import org.bson.Document;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class MongoAdapter implements IDataAdapter {

    private static List<String> ID_BLACKLIST = Arrays.asList("rpgcore", "_id");
    private static ReplaceOptions UPSERT_OPTION = new ReplaceOptions().upsert(true);

    private final MongoDatabase database;
    private MongoCollection<Document> collection_custom;
    private MongoCollection<Document> collection_roster;
    private MongoCollection<Document> collection_character;

    private boolean working;

    public MongoAdapter(String token) {
        ConnectionString connectionString = new ConnectionString(token);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString).build();
        MongoClient mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("rpgcore-player");
        this.collection_custom = this.database.getCollection("custom");
        this.collection_roster = this.database.getCollection("roster");
        this.collection_character = this.database.getCollection("character");
    }

    private static Document internalWrite(Map<String, DataBundle> bundles) {
        Document document = new Document();
        bundles.forEach((path, bundle) -> {
            document.append(path, bundle.getHandle());
        });
        return document;
    }

    private static Map<String, DataBundle> internalRead(Document document) {
        if (document == null) {
            return new HashMap<>();
        }

        Map<String, DataBundle> bundles = new HashMap<>();
        for (String key : document.keySet()) {
            if (!ID_BLACKLIST.contains(key)) {
                DataBundle bundle = new DataBundle();
                List<String> list = document.get(key, List.class);
                if (list != null) {
                    bundle.getHandle().addAll(list);
                }
                bundles.put(key, bundle);
            }
        }
        return bundles;
    }

    @Override
    public boolean isWorking() {
        return false;
    }

    @Override
    public synchronized void operateCustom(UUID uuid, String keyword, Consumer<Map<String, DataBundle>> process) {
        try {
            // load the raw data
            Map<String, DataBundle> loaded = loadCustom(uuid, keyword);
            // process the loaded data
            process.accept(loaded);
            // save our changes again
            saveCustom(uuid, keyword, loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized Map<String, DataBundle> loadCustom(UUID uuid, String keyword) throws IOException {
        Document id = new Document();
        id.append("type", "custom");
        id.append("uuid", String.valueOf(uuid));
        id.append("keyword", keyword);

        Document document = collection_custom.find(Filters.eq("_id", id)).first();

        return internalRead(document);
    }

    @Override
    public synchronized Map<String, DataBundle> loadRosterData(UUID uuid) throws IOException {
        Document id = new Document();
        id.append("type", "roster");
        id.append("uuid", String.valueOf(uuid));

        Document document = collection_roster.find(Filters.eq("_id", id)).first();

        return internalRead(document);
    }

    @Override
    public synchronized Map<String, DataBundle> loadCharacterData(UUID uuid, int character) throws IOException {
        Document id = new Document();
        id.append("type", "character");
        id.append("uuid", String.valueOf(uuid));
        id.append("slot", character);

        Document document = collection_character.find(Filters.eq("_id", id)).first();

        return internalRead(document);
    }

    @Override
    public synchronized void saveCustom(UUID uuid, String keyword, Map<String, DataBundle> data) throws IOException {
        Document id = new Document();
        id.append("type", "custom");
        id.append("uuid", String.valueOf(uuid));
        id.append("keyword", keyword);

        Document document = internalWrite(data);
        document.put("_id", id);

        collection_custom.replaceOne(Filters.eq("_id", id), internalWrite(data), UPSERT_OPTION);
    }

    @Override
    public synchronized void saveRosterData(UUID uuid, Map<String, DataBundle> data) throws IOException {
        Document id = new Document();
        id.append("type", "roster");
        id.append("uuid", String.valueOf(uuid));

        Document document = internalWrite(data);
        document.put("_id", id);

        collection_roster.replaceOne(Filters.eq("_id", id), internalWrite(data), UPSERT_OPTION);
    }

    @Override
    public synchronized void saveCharacterData(UUID uuid, int character, Map<String, DataBundle> data) throws IOException {
        Document id = new Document();
        id.append("type", "character");
        id.append("uuid", String.valueOf(uuid));
        id.append("slot", character);

        Document document = internalWrite(data);
        document.put("_id", id);

        collection_character.replaceOne(Filters.eq("_id", id), internalWrite(data), UPSERT_OPTION);
    }
}