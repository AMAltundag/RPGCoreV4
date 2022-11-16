package me.blutkrone.rpgcore.nms.api.packet.wrapper;

public enum VolatileStatus {
    BURNING(0x01),
    CROUCHING(0x02),
    RIDING(0x04),
    SPRINTING(0x08),
    SWIMMING(0x10),
    INVISIBLE(0x20),
    GLOWING(0x40),
    ELYTRA(0x80);

    private final byte id;

    VolatileStatus(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }
}
