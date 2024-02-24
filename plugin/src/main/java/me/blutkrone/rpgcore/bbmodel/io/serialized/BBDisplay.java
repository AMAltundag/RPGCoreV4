package me.blutkrone.rpgcore.bbmodel.io.serialized;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;

/**
 * Represents a display option for a BBModel
 */
public class BBDisplay {
    public float[] translation;
    public float[] rotation;
    public float[] scale;

    public BBDisplay(JsonObject bb_display) {
        this.translation = BBUtil.toFloatArray(bb_display.getAsJsonArray("translation"), 3);
        this.rotation = BBUtil.toFloatArray(bb_display.getAsJsonArray("rotation"), 3);
        this.scale = BBUtil.toFloatArray(bb_display.getAsJsonArray("scale"), 3);
    }

    public BBDisplay() {
        this.translation = new float[3];
        this.rotation = new float[3];
        this.scale = new float[3];
    }

    public BBDisplay translation(float x, float y, float z) {
        this.translation = new float[] {x, y, z};
        return this;
    }

    public BBDisplay rotation(float x, float y, float z) {
        this.rotation = new float[] {x, y, z};
        return this;
    }

    public BBDisplay scale(float x, float y, float z) {
        this.scale = new float[] {x, y, z};
        return this;
    }

    public JsonObject export() {
        JsonObject json = new JsonObject();
        json.add("translation", BBUtil.toJsonArray(this.translation));
        json.add("rotation", BBUtil.toJsonArray(this.rotation));
        json.add("scale", BBUtil.toJsonArray(this.scale));
        return json;
    }
}
