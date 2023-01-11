package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class RosterRefinementProtocol implements DataProtocol {

    @Override
    public boolean isRosterData() {
        return true;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addNumber(player.getRefinementTimestamp().size());
        player.getRefinementTimestamp().forEach((id, stamp) -> {
            bundle.addString(id);
            bundle.addNumber(stamp);
        });
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (!bundle.isEmpty()) {
            int size = bundle.getNumber(0).intValue();
            for (int i = 0; i < size; i++) {
                String id = bundle.getString(1 + (i * 2));
                long stamp = bundle.getNumber(2 + (i * 2)).longValue();
                player.getRefinementTimestamp().put(id, stamp);
            }
        }
    }
}
