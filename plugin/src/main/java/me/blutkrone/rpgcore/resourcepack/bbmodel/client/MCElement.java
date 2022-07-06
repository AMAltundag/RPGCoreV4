package me.blutkrone.rpgcore.resourcepack.bbmodel.client;

import java.util.Map;

public class MCElement {
    private final float[] from;
    private final float[] to;
    private final MCRotation rotation;
    private final Map<String, MCFace> faces;

    public MCElement(float[] from, float[] to, MCRotation rotation, Map<String, MCFace> faces) {
        this.from = from;
        this.to = to;
        this.rotation = rotation;
        this.faces = faces;
    }
}
