package me.blutkrone.rpgcore.bbmodel.io.serialized.geometry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;

import java.util.Arrays;

/**
 * Represents a face on a cube.
 */
public class BBFace {
    public float[] uv;
    public int rotation;
    public String texture;
    public int tintindex;

    /**
     * Represents a face on a cube.
     *
     * @param bb_face JSON representation.
     */
    public BBFace(JsonObject bb_face) {
        this.uv = BBUtil.toFloatArray(bb_face.getAsJsonArray("uv"), 4);
        if (bb_face.has("texture")) {
            JsonElement element = bb_face.get("texture");
            if (element != null && !element.isJsonNull()) {
                this.texture = "#" + element.getAsInt();
            }
        }
        if (bb_face.has("rotation")) {
            JsonElement element = bb_face.get("rotation");
            if (element != null && !element.isJsonNull()) {
                this.rotation = element.getAsInt();
            }
        }
        if (bb_face.has("tintindex")) {
            JsonElement element = bb_face.get("tintindex");
            if (element != null && !element.isJsonNull()) {
                this.tintindex = element.getAsInt();
            }
        }
    }

    /**
     * Internal constructor for copies.
     */
    private BBFace() {
    }

    /**
     * Normalize the face by the given width-height ratio.
     *
     * @param ratio_width Texture resolution ratio
     * @param ratio_height Texture resolution ratio
     */
    public void normalize(float ratio_width, float ratio_height) {
        this.uv[0] = this.uv[0] * ratio_width;
        this.uv[1] = this.uv[1] * ratio_height;
        this.uv[2] = this.uv[2] * ratio_width;
        this.uv[3] = this.uv[3] * ratio_height;
    }

    /**
     * Create a deep copy of this object.
     *
     * @return Deep copy of this
     */
    public BBFace copy() {
        BBFace copy = new BBFace();
        copy.uv = Arrays.copyOf(this.uv, 4);
        copy.rotation = this.rotation;
        copy.texture = this.texture;
        copy.tintindex = this.tintindex;
        return copy;
    }

    /**
     * Export this into the JSOn representation.
     *
     * @return JSON representation of this
     */
    public JsonObject export() {
        JsonObject output = new JsonObject();
        output.add("uv", BBUtil.toJsonArray(this.uv));
        output.addProperty("rotation", this.rotation);
        output.addProperty("texture", this.texture);
        output.addProperty("tintindex", this.tintindex);
        return output;
    }
}
