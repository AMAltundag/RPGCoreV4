package me.blutkrone.rpgcore.nms.api.packet.wrapper;

public enum VolatileSkin {
    CAPE(0x01),
    JACKET(0x02),
    LEFT_SLEEVE(0x04),
    RIGHT_SLEEVE(0x08),
    LEFT_PANTS(0x10),
    RIGHT_PANTS(0x20),
    HAT(0x40),
    UNUSED(0x80);

    private final byte code;

    VolatileSkin(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return code;
    }
}
