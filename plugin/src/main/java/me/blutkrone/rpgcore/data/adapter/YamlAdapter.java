package me.blutkrone.rpgcore.data.adapter;

import me.blutkrone.rpgcore.api.data.IDataAdapter;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class YamlAdapter implements IDataAdapter {
    public YamlAdapter(ConfigWrapper config) {

    }

    @Override
    public void operateCustom(UUID uuid, String keyword, Consumer<Map<String, DataBundle>> process) {
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
    public Map<String, DataBundle> loadCustom(UUID uuid, String keyword) throws IOException {
        File file = FileUtil.file("database", "custom-" + uuid + "-" + keyword + ".yml");
        ConfigWrapper config = FileUtil.asConfigYML(file);

        Map<String, DataBundle> bundles = new HashMap<>();
        config.forEachWithSelf((path, root) -> {
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
                } else if ("vector".equalsIgnoreCase(type)) {
                    bundle.addVector(raw.getVector("value"));
                } else if ("location".equalsIgnoreCase(type)) {
                    bundle.addLocation(raw.getLocation("value"));
                }

                counter += 1;
            }
            bundles.put(path, bundle);
        });
        return bundles;
    }

    @Override
    public Map<String, DataBundle> loadRosterData(UUID uuid) throws IOException {
        File file = FileUtil.file("database", "roster-" + uuid + ".yml");
        ConfigWrapper config = FileUtil.asConfigYML(file);

        Map<String, DataBundle> bundles = new HashMap<>();
        config.forEachWithSelf((path, root) -> {
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
                } else if ("vector".equalsIgnoreCase(type)) {
                    bundle.addVector(raw.getVector("value"));
                } else if ("location".equalsIgnoreCase(type)) {
                    bundle.addLocation(raw.getLocation("value"));
                }

                counter += 1;
            }
            bundles.put(path, bundle);
        });
        return bundles;
    }

    @Override
    public Map<String, DataBundle> loadCharacterData(UUID uuid, int character) throws IOException {
        File file = FileUtil.file("database", "character-" + uuid + "-" + character + ".yml");
        ConfigWrapper config = FileUtil.asConfigYML(file);

        Map<String, DataBundle> bundles = new HashMap<>();
        config.forEachWithSelf((path, root) -> {
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
                } else if ("vector".equalsIgnoreCase(type)) {
                    bundle.addVector(raw.getVector("value"));
                } else if ("location".equalsIgnoreCase(type)) {
                    bundle.addLocation(raw.getLocation("value"));
                }

                counter += 1;
            }
            bundles.put(path, bundle);
        });
        return bundles;
    }

    @Override
    public void saveCustom(UUID uuid, String keyword, Map<String, DataBundle> data) throws IOException {
        File file = FileUtil.file("database", "custom-" + uuid + "-" + keyword + ".yml");
        ConfigWrapper config = FileUtil.asConfigYML(file);

        data.forEach((path, bundle) -> {
            int counter = 0;

            for (Object datum : bundle.getHandle()) {
                if (datum instanceof Number) {
                    config.set(path + "." + counter + ".type", "number");
                    config.set(path + "." + counter + ".value", ((Number) datum).doubleValue());
                } else if (datum instanceof Boolean) {
                    config.set(path + "." + counter + ".type", "boolean");
                    config.set(path + "." + counter + ".value", ((Boolean) datum));
                } else if (datum instanceof String) {
                    config.set(path + "." + counter + ".type", "string");
                    config.set(path + "." + counter + ".value", ((String) datum));
                } else if (datum instanceof Vector) {
                    config.set(path + "." + counter + ".type", "vector");
                    config.set(path + "." + counter + ".value", ((Vector) datum).serialize());
                } else if (datum instanceof Location) {
                    config.set(path + "." + counter + ".type", "location");
                    config.set(path + "." + counter + ".value", ((Location) datum).serialize());
                }

                counter += 1;
            }
        });

        ((YamlConfiguration) config.getHandle()).save(file);
    }

    @Override
    public void saveRosterData(UUID uuid, Map<String, DataBundle> data) throws IOException {
        File file = FileUtil.file("database", "roster-" + uuid + ".yml");
        ConfigWrapper config = FileUtil.asConfigYML(file);

        data.forEach((path, bundle) -> {
            int counter = 0;

            for (Object datum : bundle.getHandle()) {
                if (datum instanceof Number) {
                    config.set(path + "." + counter + ".type", "number");
                    config.set(path + "." + counter + ".value", ((Number) datum).doubleValue());
                } else if (datum instanceof Boolean) {
                    config.set(path + "." + counter + ".type", "boolean");
                    config.set(path + "." + counter + ".value", ((Boolean) datum));
                } else if (datum instanceof String) {
                    config.set(path + "." + counter + ".type", "string");
                    config.set(path + "." + counter + ".value", ((String) datum));
                } else if (datum instanceof Vector) {
                    config.set(path + "." + counter + ".type", "vector");
                    config.set(path + "." + counter + ".value", ((Vector) datum).serialize());
                } else if (datum instanceof Location) {
                    config.set(path + "." + counter + ".type", "location");
                    config.set(path + "." + counter + ".value", ((Location) datum).serialize());
                }

                counter += 1;
            }
        });

        ((YamlConfiguration) config.getHandle()).save(file);
    }

    @Override
    public void saveCharacterData(UUID uuid, int character, Map<String, DataBundle> data) throws IOException {
        File file = FileUtil.file("database", "character-" + uuid + "-" + character + ".yml");
        ConfigWrapper config = FileUtil.asConfigYML(file);

        data.forEach((path, bundle) -> {
            int counter = 0;

            for (Object datum : bundle.getHandle()) {
                if (datum instanceof Number) {
                    config.set(path + "." + counter + ".type", "number");
                    config.set(path + "." + counter + ".value", ((Number) datum).doubleValue());
                } else if (datum instanceof Boolean) {
                    config.set(path + "." + counter + ".type", "boolean");
                    config.set(path + "." + counter + ".value", ((Boolean) datum));
                } else if (datum instanceof String) {
                    config.set(path + "." + counter + ".type", "string");
                    config.set(path + "." + counter + ".value", ((String) datum));
                } else if (datum instanceof Vector) {
                    config.set(path + "." + counter + ".type", "vector");
                    config.set(path + "." + counter + ".value", ((Vector) datum).serialize());
                } else if (datum instanceof Location) {
                    config.set(path + "." + counter + ".type", "location");
                    config.set(path + "." + counter + ".value", ((Location) datum).serialize());
                }

                counter += 1;
            }
        });

        ((YamlConfiguration) config.getHandle()).save(file);
    }

    @Override
    public void flush() {

    }
}
