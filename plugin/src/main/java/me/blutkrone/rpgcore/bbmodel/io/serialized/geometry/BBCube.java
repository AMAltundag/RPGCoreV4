package me.blutkrone.rpgcore.bbmodel.io.serialized.geometry;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a cube in the model.
 */
public class BBCube {
    public UUID uuid;
    public float[] from;
    public float[] to;
    public BBRotation rotation;
    public Map<String, BBFace> faces;
    public boolean visible;
    public String info;

    /**
     * Represents a cube in the model.
     *
     * @param bb_element JSON representation.
     */
    public BBCube(JsonObject bb_element) {
        this.uuid = UUID.fromString(bb_element.get("uuid").getAsString());
        this.faces = BBUtil.importFrom(bb_element.getAsJsonObject("faces"), BBFace::new);
        this.from = BBUtil.toFloatArray(bb_element.getAsJsonArray("from"), 3);
        this.to = BBUtil.toFloatArray(bb_element.getAsJsonArray("to"), 3);
        if (bb_element.has("visibility")) {
            this.visible = bb_element.get("visibility").getAsBoolean();
        } else {
            this.visible = true;
        }

        try {
            this.rotation = new BBRotation(bb_element);
        } catch (Exception e) {
            this.rotation = null;
        }
    }

    public BBCube(UUID uuid, float[] from, float[] to, BBRotation rotation, Map<String, BBFace> faces, boolean visible, String info) {
        this.uuid = uuid;
        this.from = from;
        this.to = to;
        this.rotation = rotation;
        this.faces = faces;
        this.visible = visible;
        this.info = info;
    }

    /*
     * Internal constructor for copies.
     */
    private BBCube() {
    }

    public float[] getFrom() {
        return from;
    }

    public float[] getTo() {
        return to;
    }

    /**
     * Inflate the cube by the given factor.
     *
     * @param inflate
     */
    public void inflate(float inflate) {
        this.from[0] = this.from[0] - inflate;
        this.from[1] = this.from[1] - inflate;
        this.from[2] = this.from[2] - inflate;
        this.to[0] = this.to[0] + inflate;
        this.to[1] = this.to[1] + inflate;
        this.to[2] = this.to[2] + inflate;
    }

    /**
     * Shrink the cube to the given factor.
     *
     * @param shrink
     */
    public void shrink(float shrink) {
        this.from[0] = this.from[0] * shrink;
        this.from[1] = this.from[1] * shrink;
        this.from[2] = this.from[2] * shrink;
        this.to[0] = this.to[0] * shrink;
        this.to[1] = this.to[1] * shrink;
        this.to[2] = this.to[2] * shrink;
        if (this.rotation != null) {
            this.rotation.pivot[0] = this.rotation.pivot[0] * shrink;
            this.rotation.pivot[1] = this.rotation.pivot[1] * shrink;
            this.rotation.pivot[2] = this.rotation.pivot[2] * shrink;
        }
    }

    /**
     * Offset the cube by the given distance.
     *
     * @param x
     * @param y
     * @param z
     */
    public void translate(float x, float y, float z) {
        this.from[0] = this.from[0] + x;
        this.from[1] = this.from[1] + y;
        this.from[2] = this.from[2] + z;
        this.to[0] = this.to[0] + x;
        this.to[1] = this.to[1] + y;
        this.to[2] = this.to[2] + z;
        if (this.rotation != null) {
            this.rotation.translate(x, y, z);
        }
    }

    /**
     * Create a deep copy of this object.
     *
     * @return Deep copy of this
     */
    public BBCube copy() {
        BBCube cube = new BBCube();
        cube.uuid = this.uuid;
        cube.from = this.from.clone();
        cube.to = this.to.clone();
        cube.visible = this.visible;
        if (this.rotation != null) {
            cube.rotation = this.rotation.copy();
        }
        cube.faces = new HashMap<>();
        this.faces.forEach((id, face) -> cube.faces.put(id, face.copy()));
        return cube;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Export this into the JSOn representation.
     *
     * @return JSON representation of this
     */
    public JsonObject export() {
        JsonObject output = new JsonObject();
        output.add("from", BBUtil.toJsonArray(from));
        output.add("to", BBUtil.toJsonArray(to));
        if (rotation != null) {
            output.add("rotation", rotation.export());
        };
        JsonObject faces = new JsonObject();
        this.faces.forEach((id, face) -> faces.add(id, face.export()));
        output.add("faces", faces);
        if (this.info != null) {
            output.addProperty("__info", this.info);
        }
        return output;
    }
}