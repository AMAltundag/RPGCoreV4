package me.blutkrone.rpgcore.quest;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.AbstractEditorQuestTask;
import me.blutkrone.rpgcore.hud.editor.root.quest.EditorQuest;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A quest has multiple tasks which have to be
 * completed sequentially.
 */
public class CoreQuest {

    // unique ID of this quest
    private String id;
    // text information about this quest
    private String lc_quest_info;
    // sequential tasks to complete for quest
    private List<AbstractQuestTask<?>> tasks = new ArrayList<>();
    // ids of quests that must have been completed
    private Set<String> required_quests = new HashSet<>();
    // ids of quests that cannot have been completed
    private Set<String> forbidden_quests = new HashSet<>();
    // always gain these rewards
    private List<AbstractQuestReward> fixed_rewards = new ArrayList<>();
    // only 1 choice reward is offered
    private List<AbstractQuestReward> choice_rewards = new ArrayList<>();
    // instantly gains all these quest on completion
    private List<String> follow_quests = new ArrayList<>();
    // instantly abandon all quests that have this ID
    private List<String> abandon_quests = new ArrayList<>();
    // reward giver for this quest
    private String reward_npc;
    // what icon to hint quest with
    private String symbol;

    public CoreQuest(String id, EditorQuest editor) {
        this.id = id;
        this.lc_quest_info = editor.lc_info;
        for (IEditorBundle bundle : editor.tasks) {
            AbstractEditorQuestTask task = (AbstractEditorQuestTask) bundle;
            this.tasks.add(task.build(this));
        }
        this.required_quests.addAll(editor.required_quest);
        this.forbidden_quests.addAll(editor.forbidden_quest);
        for (IEditorBundle bundle : editor.fixed_rewards) {
            AbstractEditorQuestReward reward = (AbstractEditorQuestReward) bundle;
            this.fixed_rewards.add(reward.build());
        }
        for (IEditorBundle bundle : editor.choice_rewards) {
            AbstractEditorQuestReward reward = (AbstractEditorQuestReward) bundle;
            this.choice_rewards.add(reward.build());
        }
        this.follow_quests.addAll(editor.follow_quest);
        this.abandon_quests.addAll(editor.abandon_quest);
        this.reward_npc = editor.npc_rewards;
        this.symbol = editor.symbol;
    }

    /**
     * The symbol used when this quest is up for picking.
     *
     * @return symbol to use when quest is available
     */
    public String getSymbol() {
        return "static_" + this.symbol + "_quest_icon";
    }

    /**
     * The NPC which offers the rewards.
     *
     * @return who offers the rewards.
     */
    public String getRewardNPC() {
        return "NOTHINGNESS".equalsIgnoreCase(this.reward_npc) ? null : this.reward_npc;
    }

    /**
     * Retrieve the unique identifier for this quest.
     *
     * @return quest identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Rewards always given for quest completion.
     *
     * @return fixed rewards.
     * @see #getCurrentTask(CorePlayer) if null we can claim rewards.
     */
    public List<AbstractQuestReward> getFixedRewards() {
        return fixed_rewards;
    }

    /**
     * Rewards of which the player has to choose only one.
     *
     * @return choice rewards.
     */
    public List<AbstractQuestReward> getChoiceRewards() {
        return choice_rewards;
    }

    /**
     * Retrieve the first quest stage which the player has
     * not finished. If no tasks remain, this will return
     * null.
     *
     * @param player who are we checking.
     * @return the task the player will work on.
     * @see #isActive(CorePlayer) to see if player has the quest.
     */
    public AbstractQuestTask getCurrentTask(CorePlayer player) {
        for (AbstractQuestTask task : tasks) {
            // completed tasks are to be marked as such
            if (player.getProgressQuests().getOrDefault("completed_" + task.getUniqueId(), 0) == 1) {
                continue;
            }
            // otherwise we check if the task is finished
            if (!task.taskIsComplete(player)) {
                return task;
            }
            // brand to avoid unnecessary computing
            player.getProgressQuests().put("completed_" + task.getUniqueId(), 1);
        }

        return null;
    }

    /**
     * Check if the player is currently working on the quest.
     *
     * @param player who is involved
     * @return true if quest is active
     */
    public boolean isActive(CorePlayer player) {
        return player.getActiveQuestIds().contains(this.id);
    }

    /**
     * Check if the player has completed the quest.
     *
     * @param player who is involved
     * @return true is quest is completed
     */
    public boolean isComplete(CorePlayer player) {
        return player.getCompletedQuests().contains(this.id);
    }

    /**
     * Accept the quest, this does NOT check if the quest can be
     * accepted or if the player has too many active quests.
     *
     * @param player whose quests are we dealing with.
     */
    public void acceptQuest(CorePlayer player) {
        // inform about taking the quest
        String message = RPGCore.inst().getLanguageManager().getTranslation("quest_accepted");
        message = message.replace("{QUEST}", this.getName());
        player.getEntity().sendMessage(message);
        // take the quest
        player.getActiveQuestIds().add(this.id);
    }

    /**
     * Complete this quest, this will ignore the actual progress
     * within the quest.
     * <p>
     * Call this after finishing all tasks and having claimed
     * the rewards!
     */
    public void completeQuest(CorePlayer player) {
        String message = RPGCore.inst().getLanguageManager().getTranslation("quest_completed");
        message = message.replace("{QUEST}", this.getName());
        player.getEntity().sendMessage(message);

        // drop this from active quests
        player.getActiveQuestIds().remove(this.id);
        // mark quest as completed
        player.getCompletedQuests().add(this.id);
        // purge all data ids we have
        for (AbstractQuestTask<?> task : this.tasks) {
            for (String id : task.getDataIds()) {
                player.getProgressQuests().remove(id);
            }
            player.getProgressQuests().remove("completed_" + task.getUniqueId());
        }
        // abandon the quests we cannot have
        for (String id : this.abandon_quests) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            quest.abandonQuest(player);
        }
        // pick up quests we should have
        for (String id : this.follow_quests) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            if (quest.canAcceptQuest(player)) {
                quest.acceptQuest(player);
            }
        }
    }

    /**
     * Abandon a quest that has been running.
     *
     * @param player
     */
    public void abandonQuest(CorePlayer player) {
        String message = RPGCore.inst().getLanguageManager().getTranslation("quest_abandoned");
        message = message.replace("{QUEST}", this.getName());
        player.getEntity().sendMessage(message);

        // drop this from active quests
        player.getActiveQuestIds().remove(this.id);
        // purge all data ids we have
        for (AbstractQuestTask<?> task : this.tasks) {
            for (String id : task.getDataIds()) {
                player.getProgressQuests().remove(id);
            }
            player.getProgressQuests().remove("completed_" + task.getUniqueId());
        }
    }

    /**
     * Check if the given quest can be granted to a player. An
     * external check should be done to avoid hand-accepting a
     * quest while having too many.
     *
     * @param player who to check against.
     * @return true if the player can take the quest.
     */
    public boolean canAcceptQuest(CorePlayer player) {
        // make sure we do not have this quest
        if (player.getActiveQuestIds().contains(this.id)) {
            return false;
        }
        // make sure we do not have a blacklisted quest
        for (String id : this.forbidden_quests) {
            if (player.getActiveQuestIds().contains(id)) {
                return false;
            }
            if (player.getCompletedQuests().contains(id)) {
                return false;
            }
        }
        // make sure we do have all whitelisted quests
        for (String id : this.required_quests) {
            if (!player.getCompletedQuests().contains(id)) {
                return false;
            }
        }
        // player can have this quest
        return true;
    }

    /**
     * Itemized icon representing this quest.
     *
     * @return quest icon.
     */
    public ItemStack getIcon() {
        return RPGCore.inst().getLanguageManager().getAsItem(this.lc_quest_info).build();
    }

    /**
     * A name for this quest.
     *
     * @return name of the quest.
     */
    public String getName() {
        List<String> translated = RPGCore.inst().getLanguageManager().getTranslationList(this.lc_quest_info);
        return translated.get(1);
    }
}
