package me.blutkrone.rpgcore.quest.task;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestTask;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.quest.CoreQuest;

import java.util.List;

/**
 * A task which the player needs to complete.
 *
 * @param <K> parameter type, depends on task.
 */
public abstract class AbstractQuestTask<K> {

    // information about this quest task
    private final String lc_info;
    private final String uuid;
    private final CoreQuest quest;

    public AbstractQuestTask(CoreQuest quest, AbstractEditorQuestTask editor) {
        this.quest = quest;
        this.lc_info = editor.getInfoLC();
        this.uuid = quest.getId() + "_" + String.valueOf(editor.getUniqueId());
    }

    /**
     * Which quest are we under.
     *
     * @return quest we are linked to.
     */
    public CoreQuest getQuest() {
        return quest;
    }

    /**
     * A distinct unique ID which is intended to track the
     * progress of a quest.
     *
     * @return unique identifier for this task.
     */
    public String getUniqueId() {
        return uuid;
    }

    /**
     * Get an information string based on the quest, please
     * cache this for a certain duration since this is does
     * generate the info on-the-fly.
     *
     * @param player whose quest state to check.
     * @return a description of this quest task.
     */
    public List<String> getInfo(CorePlayer player) {
        // fetch info for this quest task
        List<String> info = RPGCore.inst().getLanguageManager().getTranslationList(this.lc_info);
        // write quest position on player log
        int index = player.getActiveQuestIds().indexOf(this.quest.getId()) + 1;
        info.replaceAll((line -> line.replace("{quest_marker}", String.valueOf(index))));
        // offer info for further processing
        return info;
    }

    /**
     * Check if the player completed this specific task.
     *
     * @param player whose quest state to check.
     * @return true if the task is completed.
     */
    public abstract boolean taskIsComplete(CorePlayer player);

    /**
     * Update the task we are working on.
     *
     * @param player whose quest state to update
     * @param param  a distinct parameter based on the task
     */
    public abstract void updateQuest(CorePlayer player, K param);

    /**
     * All identifiers which are used by this task to
     * track the progress.
     *
     * @return unique identifier for task data.
     */
    public abstract List<String> getDataIds();
}
