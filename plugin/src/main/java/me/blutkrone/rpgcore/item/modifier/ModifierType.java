package me.blutkrone.rpgcore.item.modifier;

public enum ModifierType {
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
     * A modifier which will only be on the item scope, the
     * entity usually will not know about the modifier.
     */
    ITEM
}
