package me.blutkrone.rpgcore.item.modifier;

public enum ModifierType {
    /**
     * Modifiers only applied when using this item as a
     * weapon.
     */
    WEAPON,
    /**
     * A modifier which will affect the entity.
     */
    ENTITY,
    /**
     * A modifier which will affect the entity, once
     * the item is consumed.
     */
    CONSUMABLE,
    /**
     * A modifier which will affect the item it is on.
     */
    ITEM
}
