package me.blutkrone.rpgcore.resourcepack.bbmodel.editor;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.resourcepack.bbmodel.BBExporter;
import me.blutkrone.rpgcore.resourcepack.bbmodel.client.MCElement;
import me.blutkrone.rpgcore.resourcepack.bbmodel.client.MCFace;
import me.blutkrone.rpgcore.resourcepack.bbmodel.client.MCRotation;

import java.util.HashMap;
import java.util.Map;

public class BBElement {
    private float[] from;
    private float[] to;
    private float[] rotation;
    private float[] origin;
    private Map<String, BBFace> faces;

    public BBElement(JsonObject json) {
        this.from = BBExporter.asFloatArray(json.getAsJsonArray("from"), 0f, 0f, 0f);
        this.to = BBExporter.asFloatArray(json.getAsJsonArray("to"), 0f, 0f, 0f);
        this.rotation = BBExporter.asFloatArray(json.getAsJsonArray("rotation"), 0f, 0f, 0f);
        this.origin = BBExporter.asFloatArray(json.getAsJsonArray("origin"), 0f, 0f, 0f);
        JsonObject json_faces = json.getAsJsonObject("faces");
        this.faces = BBExporter.toMap(json_faces, (j -> new BBFace(j.getAsJsonObject())));
    }

    private String getAxis() {
        if (this.rotation.length == 0) {
            return "x";
        } else {
            if (this.rotation[0] != 0.0F) {
                return "x";
            } else if (this.rotation[1] != 0.0F) {
                return "y";
            } else {
                return this.rotation[2] != 0.0F ? "z" : "x";
            }
        }
    }

    private float getAngle() {
        if (this.rotation[0] != 0.0F) {
            return this.rotation[0];
        } else if (this.rotation[1] != 0.0F) {
            return this.rotation[1];
        } else {
            return this.rotation[2];
        }
    }

    public MCElement export(int[] texture_size) {
        Map<String, MCFace> faces = new HashMap<>();
        this.faces.forEach((k, v) -> faces.put(k, v.export(texture_size)));
        return new MCElement(from, to, new MCRotation(origin, getAxis(), getAngle()), faces);
    }
}
