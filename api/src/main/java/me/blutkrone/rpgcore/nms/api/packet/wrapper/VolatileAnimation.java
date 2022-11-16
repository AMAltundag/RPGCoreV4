package me.blutkrone.rpgcore.nms.api.packet.wrapper;

/**
 * Animation enums for player NPCs
 */
public enum VolatileAnimation {
    SWING_MAIN_ARM(0),
    TAKE_DAMAGE(1),
    LEAVE_BED(2),
    SWING_OFF_HAND(3),
    CRITICAL_EFFECT(4),
    MAGIC_CRITICAL_EFFECT(5);

    private final int id;

    VolatileAnimation(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
