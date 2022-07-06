package me.blutkrone.rpgcore.party;

import me.blutkrone.rpgcore.api.party.IActiveParty;
import me.blutkrone.rpgcore.api.party.IPartyManager;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Players that are in a party can enter dungeons
 * together and are considered allies. Parties do
 * gain exp, and can unlock special buffs as they
 * advance in playable content.
 */
public class PartyManager implements IPartyManager {

    private int n = 0;

    @Override
    public IActiveParty getPartyOf(UUID player) {
        if (n++ == 100) {
            n = 0;
            Bukkit.getLogger().severe("not implemented (get party)");
        }
        return null;
    }

    @Override
    public IActiveParty createNewParty(CorePlayer leader) {
        throw new UnsupportedOperationException("not implemented (create party)");
    }
}
