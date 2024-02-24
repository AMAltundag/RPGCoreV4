package me.blutkrone.rpgcore.nms.api.packet.grouping;

import org.bukkit.entity.Player;

/**
 * Caches a packet, allowing it to be re-sent to the
 * given set of players.
 */
public interface IDispatchPacket {
    /**
     * Sends the cached packet to the given players.
     *
     * @param players who receives the packet
     */
    void sendTo(Player... players);
}
