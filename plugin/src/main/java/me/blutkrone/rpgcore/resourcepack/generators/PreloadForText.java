package me.blutkrone.rpgcore.resourcepack.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.generation.component.FontEntry;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Preload fonts that were manually tracked.
 */
public class PreloadForText implements IGenerator {
    private static final File WORKSPACE_FONT = FileUtil.directory("resourcepack/working/assets/minecraft/textures/font");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        for (File font_file : FileUtil.buildAllFiles(WORKSPACE_FONT)) {
            String filename = font_file.getName();
            if (!filename.endsWith(".json")) continue;
            filename = filename.substring(0, filename.length() - 5);
            generation.text().register(filename, preload(font_file));
        }
    }

    /**
     * Load a JSON font file, and integrate it into existing font data.
     *
     * @param font_file JSON file with font data
     * @return Font entries that were generated
     * @throws IOException    Should something go wrong
     * @throws ParseException Should something go wrong
     */
    public static List<FontEntry> preload(File font_file) throws IOException, ParseException {
        List<FontEntry> output = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(font_file.toPath())) {
            JsonObject serialized = RPGCore.inst().getGsonUgly().fromJson(reader, JsonObject.class);
            JsonArray array = (JsonArray) serialized.get("providers");
            for (JsonElement raw_provider : array) {
                JsonObject json_provider = ((JsonObject) raw_provider);
                // initialize to default parameters
                String type = "bitmap";
                String file = "undefined";
                long ascent = 0;
                long height = -1;
                // override with json elements
                if (json_provider.has("type")) {
                    type = json_provider.get("type").getAsString();
                }
                if (json_provider.has("file")) {
                    file = json_provider.get("file").getAsString();
                }
                if (json_provider.has("ascent")) {
                    ascent = json_provider.get("ascent").getAsLong();
                }
                if (json_provider.has("height")) {
                    height = json_provider.get("height").getAsLong();
                }
                // the characters that are to be occupied
                JsonArray raw_chars = (JsonArray) json_provider.get("chars");
                List<String> chars = new ArrayList<>();
                for (Object raw_char : raw_chars)
                    chars.add((String) raw_char);
                // store for later retrieval of the computing
                output.add(new FontEntry(type, file, ascent, height, chars));
            }
        }

        return output;
    }
}
