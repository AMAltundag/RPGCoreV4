package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class RosterStorageProtocol implements DataProtocol {
    @Override
    public boolean isRosterData() {
        return true;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addNumber(player.getStoredItems().size());
        player.getStoredItems().forEach((id, volume) -> {
            bundle.addString(id);
            bundle.addString(volume);
        });
        bundle.addNumber(player.getStorageUnlocked().size());
        player.getStorageUnlocked().forEach((id, duration) -> {
            bundle.addString(id);
            bundle.addNumber(duration);
        });
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle) {
        if (!bundle.isEmpty()) {
            int header = 0;

            int size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String id = bundle.getString(header++);
                String items = bundle.getString(header++);
                player.getStoredItems().put(id, items);
            }
            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String id = bundle.getString(header++);
                long stamp = bundle.getNumber(header++).longValue();
                player.getStorageUnlocked().put(id, stamp);
            }
        }
    }
}
