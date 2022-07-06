package me.blutkrone.rpgcore.resourcepack.bbmodel.client;

public class MCFace {
    private final float[] uv;
    private final int rotation;
    private final String texture;
    private final String cullface;

    public MCFace(float[] uv, int rotation, String texture, String cullface) {
        this.uv = uv;
        this.rotation = rotation;
        this.texture = texture;
        this.cullface = cullface;
    }
}
