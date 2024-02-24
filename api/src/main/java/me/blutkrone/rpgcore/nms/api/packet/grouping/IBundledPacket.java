package me.blutkrone.rpgcore.nms.api.packet.grouping;

import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Represents a 'bundled' packet, this bundle is able to hold
 * multiple packets to be dispatched as once.
 */
public interface IBundledPacket {

    /**
     * Merge two bundles into one.
     *
     * @param other Who will receive bundles
     */
    void addToOther(IBundledPacket other);

    /**
     * Merge two bundles into one.
     *
     * @param other Who will lose bundles
     */
    void takeFromOther(IBundledPacket other);

    /**
     * Dispatch the bundled packets.
     *
     * @param players Who to dispatch to.
     */
    void dispatch(Player... players);

    /**
     * Dispatch the bundled packets.
     *
     * @param player Who to dispatch to.
     */
    void dispatch(Player player);

    /**
     * Dispatch the bundled packets.
     *
     * @param players Who to dispatch to.
     */
    void dispatch(Collection<? extends Player> players);

    /**
     * Allows you to queue up a recipient, when we flush the
     * packets are sent to all the players we've queued.
     *
     * @param players Who to queue
     */
    void queue(Player players);

    /**
     * Flush queued players, also will clear the list.
     */
    void flush();
}
