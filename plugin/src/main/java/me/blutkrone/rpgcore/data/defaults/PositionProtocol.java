package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class PositionProtocol implements DataProtocol {

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        if (player.getRespawnPosition() == null)
            return;

        bundle.addLocation(player.getLocation());
        bundle.addLocation(player.getRespawnPosition());
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle) {
        if (!bundle.isEmpty()) {
            player.setLoginPosition(bundle.getLocation(0));
            player.setRespawnPosition(bundle.getLocation(1));
        }
    }
}
