package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class DisplayProtocol implements DataProtocol {
    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addString(player.getAlias());
        bundle.addString(player.getPortrait());
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle) {
        if (!bundle.isEmpty()) {
            player.setAlias(bundle.getString(0));
            player.setPortrait(bundle.getString(1));
        } else {
            player.setAlias("nothing");
            player.setPortrait("nothing");
        }
    }
}
