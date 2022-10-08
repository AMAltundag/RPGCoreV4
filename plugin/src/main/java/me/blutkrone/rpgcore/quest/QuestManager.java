package me.blutkrone.rpgcore.quest;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorDialogue;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.quest.EditorQuest;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import org.bukkit.Bukkit;
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

        Bukkit.getLogger().severe("not implemented (quests as api)");
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
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
