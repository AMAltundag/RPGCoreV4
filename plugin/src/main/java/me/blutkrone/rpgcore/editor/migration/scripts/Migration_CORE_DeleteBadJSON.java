package me.blutkrone.rpgcore.editor.migration.scripts;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.migration.AbstractMigration;
import me.blutkrone.rpgcore.util.io.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Migration_CORE_DeleteBadJSON extends AbstractMigration {

    @Override
    public void apply() {
        // deep scan of all *.rpgcore files that may be broke
        try {
            File[] files = FileUtil.buildAllFiles(FileUtil.directory("editor"));
            for (File file : files) {
                if (file.getName().endsWith(".rpgcore")) {
                    try (FileReader reader = new FileReader(file)) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    } catch (Exception ex) {
                        file.delete();
                        RPGCore.inst().getLogger().warning("Migration deleted " + file.getPath() + ", since it was corrupted!");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
