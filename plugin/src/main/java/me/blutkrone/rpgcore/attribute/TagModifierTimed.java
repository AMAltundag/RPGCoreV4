package me.blutkrone.rpgcore.attribute;

import me.blutkrone.rpgcore.RPGCore;

public class TagModifierTimed extends TagModifier {

    private final int expire_at;

    /**
     * A tag which will expire at a given timestamp.
     *
     * @param duration how many ticks the tag lasts.
     */
    public TagModifierTimed(int duration) {
        expire_at = RPGCore.inst().getTimestamp() + duration;
    }

    @Override
    public boolean isExpired() {
        return RPGCore.inst().getTimestamp() >= expire_at || super.isExpired();
    }
}
