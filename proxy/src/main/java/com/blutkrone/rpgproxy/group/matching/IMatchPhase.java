package com.blutkrone.rpgproxy.group.matching;

import com.blutkrone.rpgproxy.RPGProxy;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.UUID;

/*
 * A common ancestor to contain every
 */
public interface IMatchPhase {

    /**
     * A unique identifier for the entire matchmaking process, this
     * identifier should persist across phase changes.
     *
     * @return Unique identifier
     */
    UUID getUniqueID();

    /**
     * Retrieve players still involved with the matchmaker.
     *
     * @return Who is still involved
     */
    Collection<UUID> getInvolved();

    /**
     * Give positive feedback to the phase for the given player.
     *
     * @param player Who gave the feedback.
     * @return What phase to update into.
     */
    IMatchPhase positive(ProxiedPlayer player);

    /**
     * Give negative feedback to the phase for the given player.
     *
     * @param player Who gave the feedback.
     * @return What phase to update into.
     */
    IMatchPhase negative(ProxiedPlayer player);

    /**
     * Broadcast the given data to all involved parties.
     *
     * @param data Data to broadcast
     */
    default void broadcast(RPGProxy proxy, byte[] data) {
        for (UUID uuid : getInvolved()) {
            proxy.sendData(uuid, data);
        }
    }
}
