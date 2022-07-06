package me.blutkrone.rpgcore.resourcepack.bbmodel.editor;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.resourcepack.bbmodel.BBExporter;

public class BBDisplay {
    private float[] translation;
    private float[] rotation;
    private float[] scale;

    public BBDisplay(JsonObject json) {
        this.translation = BBExporter.asFloatArray(json.getAsJsonArray("translation"), 0f, 0f, 0f);
        this.rotation = BBExporter.asFloatArray(json.getAsJsonArray("rotation"), 0f, 0f, 0f);
        this.scale = BBExporter.asFloatArray(json.getAsJsonArray("scale"), 1f, 1f, 1f);
    }
}
