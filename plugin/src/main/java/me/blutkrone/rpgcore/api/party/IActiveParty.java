package me.blutkrone.rpgcore.api.party;

import me.blutkrone.rpgcore.entity.IOfflineCorePlayer;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

import java.util.List;
import java.util.UUID;

/**
 * A party is used across the platform, whether to define
 * players as allies or dungeon entry.
 */
public interface IActiveParty {
    /**
     * The party leader is to be tracked as an UUID.
     *
     * @return who is the party leader.
     */
    UUID getLeaderUUID();

    /**
     * The party leader is to be tracked as an UUID.
     *
     * @param uuid who is the party leader.
     */
    void setLeader(UUID uuid);

    /**
     * Check if the given player is a member of the party.
     *
     * @param player who may be a candidate
     * @return true if the given player is a member of the party
     */
    boolean isMember(CorePlayer player);

    /**
     * The given player requested to join this party.
     *
     * @param player who wants to join.
     * @throws IllegalArgumentException if the player is in a party already
     */
    void requestToJoin(CorePlayer player);

    /**
     * The given player was invited to join this party.
     *
     * @param player who is to be invited, must not be online.
     * @throws IllegalArgumentException if the player is in a party already
     */
    void inviteToJoin(IOfflineCorePlayer player);

    /**
     * All members which are in the party, including those who
     * are offline.
     *
     * @return all members of this party.
     */
    List<IOfflineCorePlayer> getAllMembers();
}
