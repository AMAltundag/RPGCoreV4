package me.blutkrone.rpgcore.party;

import me.blutkrone.rpgcore.api.party.IActiveParty;
import me.blutkrone.rpgcore.api.party.IPartyManager;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Players in a party are considered allies, dungeons do
 * require parties to enter. The leader is allowed to add
 * new players to the party.
 */
public class PartyManager implements IPartyManager {

    @Override
    public IActiveParty getPartyOf(UUID player) {
        if (Math.random() <= 0.001d) {
            Bukkit.getLogger().severe("not implemented (get party)");
        }
        return null;
    }

    @Override
    public IActiveParty createNewParty(CorePlayer leader) {
        throw new UnsupportedOperationException("not implemented (create party)");
    }
}
