package me.blutkrone.rpgcore.resourcepack.bbmodel.editor;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.resourcepack.bbmodel.BBExporter;
import me.blutkrone.rpgcore.resourcepack.bbmodel.client.MCFace;

public class BBFace {
    private float[] uv;
    private int rotation = 0;
    private int texture;
    private String cullface;

    public BBFace(JsonObject json) {
        this.cullface = json.has("cullface") ? json.get("cullface").getAsString() : null;
        this.uv = BBExporter.asFloatArray(json.getAsJsonArray("uv"), 0f, 0f, 0f, 0f);
        this.rotation = json.has("rotation") ? json.get("rotation").getAsInt() : 0;
        try {
            this.texture = json.has("texture") ? json.get("texture").getAsInt() : 0;
        } catch (Exception e) {
            this.texture = -99248;
        }
    }

    public MCFace export(int[] texture_size) {
        float[] uv = new float[4];
        for (int i = 0; i < 4; i++)
            uv[i] = this.uv[i] * 16 / texture_size[i % 2];
        return new MCFace(uv, rotation, this.texture == -99248 ? "#missing" : "#" + texture, cullface);
    }
}
