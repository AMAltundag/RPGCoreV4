package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class JobProtocol implements DataProtocol {
    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addString(player.getRawJob());
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (!bundle.isEmpty()) {
            player.setJob(bundle.getString(0));
        }
    }
}
