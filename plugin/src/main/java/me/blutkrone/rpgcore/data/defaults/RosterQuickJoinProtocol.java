package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class RosterQuickJoinProtocol implements DataProtocol {

    @Override
    public boolean isRosterData() {
        return true;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addNumber(player.getQuickJoinSlot());
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
    }
}