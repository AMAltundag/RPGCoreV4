package me.blutkrone.rpgcore.mob.loot;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

import java.util.List;

/**
 * Base template for loot
 */
public abstract class AbstractCoreLoot {

    /**
     * Offer the reward to the given player.
     *
     * @param killed who generated the reward
     * @param killer who receives the reward
     */
    public abstract void offer(CoreEntity killed, List<CorePlayer> killer);
}