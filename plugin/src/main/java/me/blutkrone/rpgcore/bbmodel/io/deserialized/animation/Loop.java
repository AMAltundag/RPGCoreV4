package me.blutkrone.rpgcore.bbmodel.io.deserialized.animation;

/**
 * How to proceed when an animation is finished.
 */
public enum Loop {
    /**
     * When finished return back to start.
     */
    LOOP,
    /**
     * When finished, we hold at the last value.
     */
    HOLD,
    /**
     * When finished, the animation will stop.
     */
    ONCE
}
