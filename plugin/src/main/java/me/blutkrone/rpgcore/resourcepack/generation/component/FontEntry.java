package me.blutkrone.rpgcore.resourcepack.generation.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

public class FontEntry {

    public final String type;
    public final String file;
    public final long ascent;
    public final long height;
    public final List<String> chars;

    public FontEntry(String type, String file, long ascent, long height, List<String> chars) {
        this.type = type;
        this.file = file;
        this.ascent = ascent;
        this.height = height;
        this.chars = chars;
    }

    public JsonObject transform() {
        JsonObject object = new JsonObject();
        object.addProperty("type", this.type);
        object.addProperty("file", this.file);
        if (this.ascent != Long.MIN_VALUE) {
            object.addProperty("ascent", this.ascent);
        }
        if (this.height != Long.MIN_VALUE) {
            object.addProperty("height", this.height);
        }
        JsonArray array = new JsonArray();
        for (String charline : chars) {
            array.add(charline);
        }
        object.add("chars", array);
        return object;
    }

    /**
     * Create a spacing provider
     *
     * @return spacing provider
     */
    public static JsonObject getAsSpace(String symbol, int size) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "space");
        JsonObject advances = new JsonObject();
        advances.addProperty(symbol, size);
        object.add("advances", advances);
        return object;
    }

    /**
     * Create negative spacing to offset text.
     *
     * @param height The height factor
     * @param chars The characters to use
     * @return Relevant entry.
     */
    public static FontEntry getAsAmberSpace(long height, String chars) {
        return new FontEntry("bitmap", "negative_space:font/pixel.png", -32768, height, Collections.singletonList(chars));
    }
}
