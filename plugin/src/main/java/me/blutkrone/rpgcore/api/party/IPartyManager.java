package me.blutkrone.rpgcore.api.party;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * A manager dedicated to handling parties, when two players
 * are in the same party they are considered allies.
 */
public interface IPartyManager {
    /**
     * Retrieve the party of a certain player.
     *
     * @param player whose party to retrieve
     * @return the party, null if none.
     */
    default IActiveParty getPartyOf(CorePlayer player) {
        return getPartyOf(player.getUniqueId());
    }

    /**
     * Retrieve the party of a certain player.
     *
     * @param player whose party to retrieve
     * @return the party, null if none.
     */
    default IActiveParty getPartyOf(OfflinePlayer player) {
        return getPartyOf(player.getUniqueId());
    }

    /**
     * Retrieve the party of a certain player.
     *
     * @param player whose party to retrieve
     * @return the party, null if none.
     */
    IActiveParty getPartyOf(UUID player);

    /**
     * Create a new party with a leader.
     *
     * @param leader who will lead the newly created party
     * @return the newly created party
     * @throws IllegalStateException thrown should the leader be in a party already.
     */
    IActiveParty createNewParty(CorePlayer leader);
}
