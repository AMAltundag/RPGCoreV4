package me.blutkrone.rpgcore.editor.migration.scripts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.migration.AbstractMigration;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/*
 * Holograms received additional information.
 */
public class Migration_0002_HologramExtraParameters extends AbstractMigration {
    @Override
    public void apply() {
        // deep scan of all *.rpgcore files with potential bundles
        Map<File, JsonObject> working = new HashMap<>();
        try {
            File[] files = FileUtil.buildAllFiles(FileUtil.directory("editor"));
            for (File file : files) {
                if (file.getName().endsWith(".rpgcore")) {
                    try (FileReader reader = new FileReader(file)) {
                        working.put(file, GSON.fromJson(reader, JsonObject.class));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // only migrate files if below a certain version
        working.entrySet().removeIf(entry -> {
            JsonElement version = entry.getValue().get("migration_version");
            return !(version == null || version.getAsInt() <= 1);
        });

        // perform the migration on the files
        if (!working.isEmpty()) {
            for (Map.Entry<File, JsonObject> working_entry : working.entrySet()) {
                // add missing parameters
                if (!working_entry.getValue().has("pitch")) {
                    working_entry.getValue().addProperty("pitch", 0f);
                }
                if (!working_entry.getValue().has("yaw")) {
                    working_entry.getValue().addProperty("yaw", 0f);
                }
                if (!working_entry.getValue().has("locked")) {
                    working_entry.getValue().addProperty("locked", false);
                }
                // mark the file as being corrected
                working_entry.getValue().addProperty("migration_version", 2);
            }

            // write the files back to the disk
            RPGCore.inst().getLogger().warning("Migrated " + working.size() + " files with " + this.getClass().getSimpleName() + "!");
            for (Map.Entry<File, JsonObject> entry : working.entrySet()) {
                try (FileWriter fw = new FileWriter(entry.getKey(), Charset.forName("UTF-8"))) {
                    GSON.toJson(entry.getValue(), fw);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
