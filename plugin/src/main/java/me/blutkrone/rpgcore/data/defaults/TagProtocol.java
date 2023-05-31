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
    public int getDataVersion() {
        return 1;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addNumber(player.getPersistentTags().size());
        for (String tag : player.getPersistentTags()) {
            bundle.addString(tag);
        }
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (!bundle.isEmpty()) {
            if (version == 0) {
                int header = 0;
                int size = bundle.getNumber(header++).intValue();
                for (int i = 0; i < size; i++) {
                    String string = bundle.getString(header++);
                    if (string.startsWith("quest") && !string.startsWith("quest_")) {
                        string = "quest_" + string.substring(5);
                    }
                    player.getPersistentTags().add(string);
                }

            } else if (version == 1) {
                int header = 0;
                int size = bundle.getNumber(header++).intValue();
                for (int i = 0; i < size; i++) {
                    player.getPersistentTags().add(bundle.getString(header++));
                }
            }
        }
    }
}
