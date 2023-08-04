package me.blutkrone.rpgcore.editor.migration.scripts;

import com.google.gson.JsonArray;
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

public class Migration_0002_VisitTaskNowNode  extends AbstractMigration {
    @Override
    public void apply() {
        // deep scan of all *.rpgcore files with potential bundles
        Map<File, JsonObject> working = new HashMap<>();
        try {
            File[] files = FileUtil.buildAllFiles(FileUtil.directory("editor/quest"));
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
            return !(version == null || version.getAsInt() <= 2);
        });

        // perform the migration on the files
        if (!working.isEmpty()) {
            for (Map.Entry<File, JsonObject> working_entry : working.entrySet()) {
                JsonElement traits = working_entry.getValue().get("tasks");
                if (traits != null) {
                    for (JsonElement element : traits.getAsJsonArray()) {
                        String clazz = element.getAsJsonObject().get("type").getAsString();
                        if (clazz.equals("me.blutkrone.rpgcore.editor.bundle.quest.task.EditorQuestTaskVisit")) {
                            JsonObject data = element.getAsJsonObject().get("data").getAsJsonObject();

                            JsonArray updated = new JsonArray();

                            JsonArray arr = data.remove("hotspots").getAsJsonArray();
                            for (int i = 0; i < arr.size(); i++) {
                                updated.add("hotspot:" + arr.get(i).getAsString());
                            }
                            data.add("nodes", updated);
                        }
                    }
                }

                // mark the file as being corrected
                working_entry.getValue().addProperty("migration_version", 3);
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