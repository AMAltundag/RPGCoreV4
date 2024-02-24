package me.blutkrone.rpgcore.bbmodel.io.serialized.animation;

import com.google.gson.JsonObject;

/**
 * A keyframe is intended to modify a model in some shape or form.
 */
public class BBKeyFrame {
    public float timestamp;
    public String interpolation;
    public float[] data_point;

    BBKeyFrame(JsonObject bb_keyframe) {
        this.timestamp = bb_keyframe.get("time").getAsFloat();
        this.interpolation = bb_keyframe.get("interpolation").getAsString();

        JsonObject header = bb_keyframe.getAsJsonArray("data_points").get(0).getAsJsonObject();
        this.data_point = new float[]{
                header.get("x").getAsFloat(),
                header.get("y").getAsFloat(),
                header.get("z").getAsFloat()
        };
    }
}
