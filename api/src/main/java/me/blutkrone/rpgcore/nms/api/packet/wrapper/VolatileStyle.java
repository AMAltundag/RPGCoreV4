package me.blutkrone.rpgcore.nms.api.packet.wrapper;

public enum VolatileStyle {
    HAS_SHADOW((byte) 0x01),
    IS_SEE_THOROUGH((byte) 0x02),
    DEFAULT_COLOR((byte) 0x04),
    CENTERED((byte) 0x00),
    LEFT((byte) 0x08),
    RIGHT((byte) 0xF0);

    public byte flag;

    VolatileStyle(byte flag) {
        this.flag = flag;
    }

    /**
     * Join the given styles into a flag.
     *
     * @param styles Styles to join
     * @return representing flag
     */
    public static byte join(VolatileStyle[] styles) {
        byte flag = 0x00;
        for (VolatileStyle style : styles) {
            flag |= style.flag;
        }
        return flag;
    }
}