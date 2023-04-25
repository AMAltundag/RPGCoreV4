package me.blutkrone.rpgcore.nms.api.packet.handle;

import org.bukkit.entity.Player;

/**
 * A highlight created at a specific block, this is
 * always created at a specific location and cannot
 * move.
 */
public interface IHighlight {

    /**
     * Enable the glow effect.
     *
     * @param player Who is affected.
     */
    void enable(Player player);

    /**
     * Disable the glow effect.
     *
     * @param player Who is affected.
     */
    void disable(Player player);
}
