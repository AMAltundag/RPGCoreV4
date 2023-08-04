package me.blutkrone.rpgcore.item.type;

public enum ItemType {
    /**
     * LMB: melee auto-attack.
     * RMB: Damaging forward shove, breaks armor.
     */
    HEAVY,

    /**
     * LMB: melee auto-attack.
     * RMB: Throw copy of weapon.
     */
    THROW,

    /**
     * LMB: Attacks hit twice.
     * RMB: Damaging backward jump, knocks enemy away.
     */
    DUAL,

    /**
     * LMB: Quick, far distance, spell damage projectile.
     * RMB: Slow, short range, spell damage blast.
     */
    MAGIC,

    /**
     * LMB: Throws item that will explode on impact.
     * RMB: Nothing
     */
    BOMB,

    /**
     * LMB: Nothing
     * RMB: Item is consumed and grants attributes.
     */
    CONSUME,

    /**
     * LMB: Nothing
     * RMB: Nothing
     * <br>
     * Auto recovering projectile for off-hand, item must be an arrow.
     */
    QUIVER,

    /**
     * LMB: Nothing
     * RMB: Active blocking behaviour
     * <br>
     * Active blocking shield for off-hand, item must be a shield.
     */
    SHIELD,

    /**
     * Item will have no special properties.
     */
    NONE
}
