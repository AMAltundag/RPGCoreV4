package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class PositionProtocol implements DataProtocol {

    @Override
    public int getDataVersion() {
        return 1;
    }

    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        if (player.getRespawnPosition() == null)
            return;

        bundle.addLocation(player.getLocation());
        bundle.addLocation(player.getRespawnPosition());
        bundle.addBoolean(player.isDying());
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (bundle.isEmpty()) {
            return;
        }

        if (version == 0) {
            player.setLoginPosition(bundle.getLocation(0));
            player.setRespawnPosition(bundle.getLocation(1));
        } else if (version == 1) {
            player.setLoginPosition(bundle.getLocation(0));
            player.setRespawnPosition(bundle.getLocation(1));
            player.setForceRespawn(bundle.getBoolean(2));
        }
    }
}
