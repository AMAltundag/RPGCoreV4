package me.blutkrone.rpgcore.attribute;

public interface IExpiringModifier {

    /**
     * Mark the modifier as being expired, making it no longer
     * available for whatever collection holds it.
     */
    void setExpired();

    /**
     * Check if this modifier was expired.
     *
     * @return true if we are expired
     */
    boolean isExpired();
}
