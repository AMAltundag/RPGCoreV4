package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

public class QuestProtocol implements DataProtocol {
    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addNumber(player.getCompletedQuests().size());
        for (String tag : player.getCompletedQuests()) {
            bundle.addString(tag);
        }

        bundle.addNumber(player.getActiveQuestIds().size());
        for (String id : player.getActiveQuestIds()) {
            bundle.addString(id);
        }

        bundle.addNumber(player.getProgressQuests().size());
        player.getProgressQuests().forEach((id, progress) -> {
            bundle.addString(id);
            bundle.addNumber(progress);
        });
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (!bundle.isEmpty()) {
            int header = 0;

            int size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                player.getCompletedQuests().add(bundle.getString(header++));
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                player.getActiveQuestIds().add(bundle.getString(header++));
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String id = bundle.getString(header++);
                int val = bundle.getNumber(header++).intValue();
                player.getProgressQuests().put(id, val);
            }
        }
    }
}
