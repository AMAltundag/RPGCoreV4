package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class TagProtocol implements DataProtocol {
    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addNumber(player.getPersistentTags().size());
        for (String tag : player.getPersistentTags()) {
            bundle.addString(tag);
        }
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle) {
        if (!bundle.isEmpty()) {
            int header = 0;
            int size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                player.getPersistentTags().add(bundle.getString(header++));
            }
        }
    }
}
