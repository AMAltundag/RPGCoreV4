package me.blutkrone.rpgcore.resourcepack.component;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

public class ResourcePackFont {
    public final String type;
    public final String file;
    public final long ascent;
    public final long height;
    public final List<String> chars;

    public ResourcePackFont(String type, String file, long ascent, long height, List<String> chars) {
        this.type = type;
        this.file = file;
        this.ascent = ascent;
        this.height = height;
        this.chars = chars;
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase();
    }

    public JSONObject transform() {
        JSONObject object = new JSONObject();
        object.put("type", this.type);
        object.put("file", this.file);
        if (this.ascent != Long.MIN_VALUE) object.put("ascent", this.ascent);
        if (this.height != Long.MIN_VALUE) object.put("height", this.height);
        JSONArray array = new JSONArray();
        for (String charline : chars)
            array.add(charline);
        object.put("chars", array);
        return object;
    }
}
