package me.blutkrone.rpgcore.quest;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.other.EditorDialogue;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.quest.EditorQuest;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskCollect;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskLogic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * This must be implemented over the API!
 */
public class QuestManager implements Listener {

    private EditorIndex<CoreQuest, EditorQuest> index_quest;
    private EditorIndex<CoreDialogue, EditorDialogue> index_dialogue;

    public QuestManager() {
        this.index_quest = new EditorIndex<>("quest", EditorQuest.class, EditorQuest::new);
        this.index_dialogue = new EditorIndex<>("dialogue", EditorDialogue.class, EditorDialogue::new);

        RPGCore.inst().getLogger().info("not implemented (quests as api)");
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            // check if collect tasks were completed
            for (Player player : Bukkit.getOnlinePlayers()) {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                if (core_player != null) {
                    for (String questId : core_player.getActiveQuestIds()) {
                        CoreQuest quest = getIndexQuest().get(questId);
                        AbstractQuestTask task = quest.getCurrentTask(core_player);
                        if (task instanceof CoreQuestTaskCollect) {
                            if (((CoreQuestTaskCollect) task).canMeetDemand(core_player)) {
                                task.updateQuest(core_player, new Object());
                            }
                        } else if (task instanceof CoreQuestTaskLogic) {
                            task.updateQuest(core_player, new Object());
                        }
                    }
                }
            }
        }, 0, 20);
    }

    /**
     * An index tracking all quests.
     *
     * @return quest index.
     */
    public EditorIndex<CoreQuest, EditorQuest> getIndexQuest() {
        return index_quest;
    }

    /**
     * An index tracking all dialogue.
     *
     * @return dialogue index.
     */
    public EditorIndex<CoreDialogue, EditorDialogue> getIndexDialogue() {
        return index_dialogue;
    }
}
