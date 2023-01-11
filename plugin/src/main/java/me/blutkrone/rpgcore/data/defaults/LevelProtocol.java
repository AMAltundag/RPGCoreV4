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
        bundle.addNumber(player.getProfessionLevel().size());
        player.getProfessionLevel().forEach((key, value) -> {
            bundle.addString(key);
            bundle.addNumber(value);
        });
        bundle.addNumber(player.getProfessionExp().size());
        player.getProfessionExp().forEach((key, value) -> {
            bundle.addString(key);
            bundle.addNumber(value);
        });
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (!bundle.isEmpty()) {
            player.setCurrentLevel(bundle.getNumber(0).intValue());
            player.setCurrentExp(bundle.getNumber(1).doubleValue());
            int header = 2;
            int size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                int value = bundle.getNumber(header++).intValue();
                player.getProfessionLevel().put(key, value);
            }
            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                double value = bundle.getNumber(header++).doubleValue();
                player.getProfessionExp().put(key, value);
            }
        }
    }
}
