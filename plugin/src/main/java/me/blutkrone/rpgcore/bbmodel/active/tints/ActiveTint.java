package me.blutkrone.rpgcore.bbmodel.active.tints;

/**
 * Represents an active 'tint' on the entity, do note
 * that updating a tint with the same ID will make it
 * so that the duration is refreshed.
 */
public class ActiveTint {
    // color of the tinted element
    public final int color;
    // only highest priority tint is shown
    public final int priority;
    // duration left on the tint
    public int duration;

    /**
     * A tint on the model, do note that a tint is applied to a
     * bone and subsequently inherited by all children.
     *
     * @param priority Priority of the tint.
     * @param color Color of the tint
     */
    public ActiveTint(int priority, int color, int duration) {
        this.priority = priority;
        this.color = color;
        this.duration = duration;
    }
}
