package me.blutkrone.rpgcore.quest.task.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorTalk;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.task.EditorQuestTaskTalk;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interact with an NPC, and present a dialogue. The task is
 * completed once you went thorough with the entire dialogue
 * chain/branch.
 */
public class CoreQuestTaskTalk extends AbstractQuestTask<CoreDialogue> {

    // all dialogues we are awaiting
    private Map<String, String> talks = new HashMap<>();

    public CoreQuestTaskTalk(CoreQuest quest, EditorQuestTaskTalk editor) {
        super(quest, editor);

        for (IEditorBundle talk : editor.talks) {
            EditorTalk e = (EditorTalk) talk;
            this.talks.put(e.npc, e.dialogue);
        }
    }

    @Override
    public List<String> getInfo(CorePlayer player) {
        List<String> info = super.getInfo(player);
        talks.forEach((npc, dialogue) -> {
            int finished = player.getProgressQuests().getOrDefault(super.getUniqueId() + "_" + npc, 0);
            info.replaceAll((line -> line.replace("{" + npc + "}", finished == 1 ? "§a1/1" : "§c0/1")));
        });
        return info;
    }

    @Override
    public boolean taskIsComplete(CorePlayer player) {
        for (String npc : this.talks.keySet()) {
            int finished = player.getProgressQuests().getOrDefault(super.getUniqueId() + "_" + npc, 0);
            if (finished != 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateQuest(CorePlayer player, CoreDialogue param) {
        throw new UnsupportedOperationException("Talk trait is processed externally.");
    }

    @Override
    public List<String> getDataIds() {
        List<String> ids = new ArrayList<>();
        this.talks.forEach((npc, dialogue) -> {
            ids.add(super.getUniqueId() + "_" + npc);
        });
        return ids;
    }

    /**
     * Retrieve the dialogue which is open.
     *
     * @param player whose dialogue we are checking
     * @return a dialogue that is available
     */
    public QuestDialogue getWaiting(CorePlayer player, CoreNPC npc) {
        // search for any unclaimed dialogue with the given NPC
        for (Map.Entry<String, String> entry : this.talks.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(npc.getId())) {
                if (player.getProgressQuests().getOrDefault(this.getUniqueId() + "_" + entry.getKey(), 0) == 0) {
                    return new QuestDialogue(entry.getKey(), RPGCore.inst().getQuestManager().getIndexDialogue().get(entry.getValue()), this);
                }
            }
        }
        // no dialogue for this NPC
        return null;
    }

    public class QuestDialogue {
        public final String npc;
        public final CoreDialogue dialogue;
        public final CoreQuestTaskTalk task;

        public QuestDialogue(String npc, CoreDialogue dialogue, CoreQuestTaskTalk quest) {
            this.npc = npc;
            this.dialogue = dialogue;
            this.task = quest;
        }
    }
}