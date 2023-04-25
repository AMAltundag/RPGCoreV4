package me.blutkrone.rpgcore.api.social;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * This is meant to be a read-only view of the party, there is no
 * guarantee on the snapshot being up-to-date.
 */
public interface IPartySnapshot {

    /**
     * A unique identifier for the party.
     *
     * @return unique identifier.
     */
    String getId();

    /**
     * The leader of the party.
     *
     * @return UUID of the party leader.
     */
    UUID getLeaderUUID();

    /**
     * Check if a player is a member of the party.
     *
     * @param player Potential member of the party
     * @return Whether the player is a member or not
     */
    boolean isMember(CorePlayer player);

    /**
     * Players online on the current server.
     *
     * @return players on current server.
     */
    List<CorePlayer> getAllOnlineMembers();

    /**
     * All players who are in this specific party.
     *
     * @return all members of the party.
     */
    List<OfflinePlayer> getAllMembers();

    /**
     * Check if given player is the leader of this party.
     *
     * @param viewer Who to check
     * @return Whether player leads this party
     */
    default boolean isLeader(Player viewer) {
        return getLeaderUUID().equals(viewer.getUniqueId());
    }
}
