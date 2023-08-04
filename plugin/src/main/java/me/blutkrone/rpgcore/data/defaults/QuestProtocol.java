package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.quest.CoreQuest;

import java.util.HashMap;
import java.util.Map;

public class QuestProtocol implements DataProtocol {
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
        bundle.addNumber(player.getCompletedQuests().size());
        for (String tag : player.getCompletedQuests()) {
            bundle.addString(tag);
        }

        bundle.addNumber(player.getActiveQuestIds().size());
        for (String quest_id : player.getActiveQuestIds()) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(quest_id);
            // track quest ID
            bundle.addString(quest_id);
            // track iteration acquired at
            bundle.addNumber(quest.getIteration());
            // identify what progress is related to this quest
            Map<String, Integer> quest_progress = new HashMap<>();
            player.getProgressQuests().forEach((progress_id, progress) -> {
                if (progress_id.startsWith(quest.getId() + "#")) {
                    quest_progress.put(progress_id, progress);
                }
            });
            // track progress for this quest
            bundle.addNumber(quest_progress.size());
            quest_progress.forEach((key, value) -> {
                bundle.addString(key);
                bundle.addNumber(value);
            });
        }
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (bundle.isEmpty()) {

        } else if (version == 0) {
            int header = 0;

            int size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                player.getCompletedQuests().add(bundle.getString(header++));
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String quest_id = bundle.getString(header++);
                int iteration = bundle.getNumber(header++).intValue();
                if (RPGCore.inst().getQuestManager().getIndexQuest().get(quest_id).getIteration() == iteration) {
                    int progress_size = bundle.getNumber(header++).intValue();
                    for (int j = 0; j < progress_size; j++) {
                        String progress_id = bundle.getString(header++);
                        int progress_value = bundle.getNumber(header++).intValue();
                        player.getProgressQuests().put(progress_id, progress_value);
                    }
                }

                player.getActiveQuestIds().add(quest_id);
            }
        } else if (version == 1) {

        }
    }
}
