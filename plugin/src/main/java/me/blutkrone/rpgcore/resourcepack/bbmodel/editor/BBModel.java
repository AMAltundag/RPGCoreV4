package me.blutkrone.rpgcore.resourcepack.bbmodel.editor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.resourcepack.bbmodel.BBExporter;
import me.blutkrone.rpgcore.resourcepack.bbmodel.client.MCElement;
import me.blutkrone.rpgcore.resourcepack.bbmodel.client.MCModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BBModel {
    private boolean boxUV;
    private String parent;
    private String name;
    private int width;
    private int height;
    private List<BBElement> elements;
    private List<BBTexture> textures;
    private Map<String, BBDisplay> display;

    public BBModel(JsonObject json) {
        this.boxUV = json.getAsJsonObject("meta").get("box_uv").getAsBoolean();
        this.name = json.has("name") ? json.get("name").getAsString() : null;
        this.parent = json.has("parent") ? json.get("parent").getAsString() : null;

        if (json.has("resolution")) {
            this.width = json.getAsJsonObject("resolution").get("width").getAsInt();
            this.height = json.getAsJsonObject("resolution").get("height").getAsInt();
        }

        JsonArray json_elements = json.getAsJsonArray("elements");
        this.elements = BBExporter.toArray(json_elements, (j -> new BBElement(j.getAsJsonObject())));

        JsonArray json_textures = json.getAsJsonArray("textures");
        this.textures = BBExporter.toArray(json_textures, (j -> new BBTexture(j.getAsJsonObject())));

        JsonObject json_display = json.getAsJsonObject("display");
        this.display = BBExporter.toMap(json_display, (j -> new BBDisplay(j.getAsJsonObject())));
    }

    public boolean isBoxUV() {
        return boxUV;
    }

    public boolean hasTexture() {
        return !this.textures.isEmpty();
    }

    public List<BBTexture> texture() {
        return this.textures;
    }

    public MCModel export() {
        int[] texture_size = new int[]{width, height};
        Map<String, String> textures = new HashMap<>();
        List<MCElement> elements = new ArrayList<>();
        this.textures.forEach(t -> textures.put(String.valueOf(t.getId()), "generated/bbmodel_" + t.getFileId()));
        this.elements.forEach(e -> elements.add(e.export(texture_size)));
        return new MCModel(name, parent, texture_size, textures, elements, display);
    }
}
