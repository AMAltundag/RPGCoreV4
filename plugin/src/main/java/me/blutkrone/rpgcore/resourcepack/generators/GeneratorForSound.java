package me.blutkrone.rpgcore.resourcepack.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom sounds
 */
public class GeneratorForSound implements IGenerator {
    private static final File WORKSPACE_SOUNDS = FileUtil.directory("resourcepack/working/assets/minecraft/sounds/custom");
    private static final File INPUT_SOUND = FileUtil.directory("resourcepack/input/sound");
    private static final File WORKSPACE_SOUND_FILE = FileUtil.file("resourcepack/working/assets/minecraft/sounds.json");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        // generate the sound specific rules
        Set<String> generated = new HashSet<>();
        JsonObject sounds = new JsonObject();
        for (File file : FileUtil.buildAllFiles(INPUT_SOUND)) {
            String name = file.getName().split("\\.")[0];
            // ensure no duplicate sounds
            if (!generated.add(name))
                continue;
            // move to relevant directory
            FileUtils.copyFile(file, new File(WORKSPACE_SOUNDS, file.getName()));
            // offer the sound to the json structure
            JsonObject sound = new JsonObject();
            sound.addProperty("category", "master");
            JsonArray array = new JsonArray();
            array.add("custom/"+name);
            sound.add("sounds", array);
            // register to the larger file
            sounds.add("generated." + name, sound);
        }
        // register the sounds we have added
        generation.write(WORKSPACE_SOUND_FILE, sounds);
    }
}
