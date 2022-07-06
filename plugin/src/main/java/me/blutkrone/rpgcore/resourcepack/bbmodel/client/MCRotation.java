package me.blutkrone.rpgcore.resourcepack.bbmodel.client;

public class MCRotation {
    private final float[] origin;
    private final String axis;
    private final float angle;

    public MCRotation(float[] origin, String axis, float angle) {
        this.origin = origin;
        this.axis = axis;
        this.angle = angle;
    }
}
