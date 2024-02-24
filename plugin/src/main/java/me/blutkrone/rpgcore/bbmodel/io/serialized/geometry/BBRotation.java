package me.blutkrone.rpgcore.bbmodel.io.serialized.geometry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;
import org.bukkit.Bukkit;

import java.util.Arrays;

/**
 * Single axis rotation of base model.
 */
public class BBRotation {
    public float[] pivot;
    public float angle;
    public String axis;

    /**
     * Single axis rotation of base model.
     *
     * @param bb_element
     */
    public BBRotation(JsonObject bb_element) {
        JsonArray bb_rotation = (JsonArray) bb_element.get("rotation");
        JsonArray bb_origin = (JsonArray) bb_element.get("origin");

        if (bb_rotation == null || bb_origin == null) {
            throw new IllegalArgumentException("Element has no rotation!");
        }

        // angles in the rotation
        float x = bb_rotation.get(0).getAsFloat();
        float y = bb_rotation.get(1).getAsFloat();
        float z = bb_rotation.get(2).getAsFloat();
        // float x = (float) Math.abs(Math.toRadians(bb_rotation.get(0).getAsFloat()));
        // float y = (float) Math.abs(Math.toRadians(bb_rotation.get(1).getAsFloat()));
        // float z = (float) Math.abs(Math.toRadians(bb_rotation.get(2).getAsFloat()));

        // warn about being unable to properly convert an angle
        if ((x != 0.0f && y != 0.0f) || (z != 0.0f && y != 0.0f) || (x != 0.0f && z != 0.0f)) {
            Bukkit.getLogger().severe("Bad rotation [%s,%s,%s] in bbmodel".formatted(x, y, z));
        }

        // base model can only rotate on one axis
        if (x != 0.0f) {
            angle = x;
            axis = "x";
        } else if (y != 0.0f) {
            angle = y;
            axis = "y";
        } else if (z != 0.0f) {
            angle = z;
            axis = "z";
        }

        // base model can only rotate in 22.5 degree increments
        angle = ((int) (angle / 22.5f)) * 22.5f;
        // angle = (int) (angle * 10.0F) / 10.0F;
        // if (Math.abs(angle) > 45.0F || angle % 22.5D != 0.0D) {
        //     Bukkit.getLogger().severe("Bad rotation %s in bbmodel".formatted(angle));
        // }
        // angle = (int) (Utility.clamp(angle, -45.0F, 45.0F) / 22.5D) * 22.5F;

        // origin is the pivot we rotate from
        pivot = new float[3];
        pivot[0] = bb_origin.get(0).getAsFloat();
        pivot[1] = bb_origin.get(1).getAsFloat();
        pivot[2] = bb_origin.get(2).getAsFloat();
    }

    /*
     * Internal constructor for copies.
     */
    private BBRotation() {
    }

    /**
     * Create a deep copy of this object.
     *
     * @return Deep copy of this
     */
    public BBRotation copy() {
        BBRotation copy = new BBRotation();
        copy.pivot = Arrays.copyOf(this.pivot, 3);
        copy.angle = this.angle;
        copy.axis = this.axis;
        return copy;
    }

    /**
     * Export this into the JSOn representation.
     *
     * @return JSON representation of this
     */
    public JsonObject export() {
        JsonObject output = new JsonObject();
        output.addProperty("axis", axis);
        output.addProperty("angle", angle);
        output.add("origin", BBUtil.toJsonArray(pivot));
        return output;
    }

    /**
     * Shrink the rotation pivot.
     *
     * @param shrink
     */
    public void shrink(float shrink) {
        this.pivot[0] = this.pivot[0] * shrink;
        this.pivot[1] = this.pivot[1] * shrink;
        this.pivot[2] = this.pivot[2] * shrink;
    }

    /**
     * Offset the rotation pivot.
     *
     * @param x
     * @param y
     * @param z
     */
    public void translate(float x, float y, float z) {
        this.pivot[0] = this.pivot[0] + x;
        this.pivot[1] = this.pivot[1] + y;
        this.pivot[2] = this.pivot[2] + z;
    }
}
