package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class LevelProtocol implements DataProtocol {
    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addNumber(player.getCurrentLevel());
        bundle.addNumber(player.getCurrentExp());
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle) {
        if (!bundle.isEmpty()) {
            player.setCurrentLevel(bundle.getNumber(0).intValue());
            player.setCurrentExp(bundle.getNumber(1).doubleValue());
        }
    }
}
