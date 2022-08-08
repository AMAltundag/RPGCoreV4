package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorQuestTrait;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskDeliver;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskTalk;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A trait intended for progression in a quest, this trait is treated
 * differently then other traits - allowing it to supersede any other
 * traits.
 */
public class CoreQuestTrait extends AbstractCoreTrait {

    // all quests which we do offer
    private Set<String> quests = new LinkedHashSet<>();

    public CoreQuestTrait(EditorQuestTrait editor) {
        super(editor);

        this.quests.addAll(editor.quests);
    }

    @Override
    public boolean isAvailable(CorePlayer player) {
        return super.isAvailable(player)
                && !getQuestAvailable(player).isEmpty();
    }

    /**
     * All quests which are offered by this NPC.
     *
     * @return offered quests.
     */
    public Set<String> getQuests() {
        return quests;
    }

    /**
     * Retrieve the quest that was completed, and is waiting
     * for rewards to be claimed.
     *
     * @param player   whose quests to check
     * @param involved which NPC was the trait on
     * @return any quest we do offer rewards for
     */
    public CoreQuest getQuestUnclaimed(Player player, CoreNPC involved) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return null;
        }

        // search for any quest that has no more tasks (finished)
        for (String id : core_player.getActiveQuestIds()) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            // ensure this NPC can actually offer the rewards
            String npc = quest.getRewardNPC();
            if (npc == null) {
                if (!this.quests.contains(id)) {
                    continue;
                }
            } else if (!npc.equalsIgnoreCase(involved.getId())) {
                continue;
            }

            // handle rewards for this NPC
            if (quest.getCurrentTask(core_player) == null) {
                return quest;
            }
        }

        // this NPC has no quests to claim
        return null;
    }

    /**
     * Retrieve quest dialogue awaiting this player.
     *
     * @param player   whose quests to check
     * @param involved which NPC was the trait on
     * @return any quest we need to process dialogue for
     */
    public CoreQuestTaskTalk.QuestDialogue getQuestDialogue(Player player, CoreNPC involved) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return null;
        }

        // search for any dialogue which is pending
        for (String id : core_player.getActiveQuestIds()) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            AbstractQuestTask task = quest.getCurrentTask(core_player);
            if (task instanceof CoreQuestTaskTalk) {
                CoreQuestTaskTalk.QuestDialogue dialogue = ((CoreQuestTaskTalk) task).getWaiting(core_player, involved);
                if (dialogue != null) {
                    return dialogue;
                }
            }
        }

        // no dialogue is pending.
        return null;
    }

    /**
     * Retrieve quest delivery awaiting this player.
     *
     * @param player   whose quests to check
     * @param involved which NPC was the trait on
     * @return any quest we need to process delivery for
     */
    public CoreQuestTaskDeliver getQuestDelivery(Player player, CoreNPC involved) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return null;
        }

        // search for any dialogue which is pending
        for (String id : core_player.getActiveQuestIds()) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            // ensure we got a delivery task
            AbstractQuestTask task = quest.getCurrentTask(core_player);
            if (!(task instanceof CoreQuestTaskDeliver)) {
                continue;
            }
            // ensure the NPC accepts drop-offs for that task
            CoreQuestTaskDeliver delivery = (CoreQuestTaskDeliver) task;
            if (!delivery.isDropOff(involved)) {
                continue;
            }
            // ensure the player meets the demand
            if (!delivery.canMeetDemand(player)) {
                continue;
            }
            // the quest has to be completed first
            return delivery;
        }

        // no dialogue is pending.
        return null;
    }

    /**
     * A listing of all quests available to be claimed.
     *
     * @param player whose quests to check
     * @return a list of all quests to claim
     */
    public List<CoreQuest> getQuestAvailable(CorePlayer player) {
        List<CoreQuest> available = new ArrayList<>();

        // search all quests now up for picking
        for (String id : this.quests) {
            // ignore active quests
            if (player.getActiveQuestIds().contains(id)) {
                continue;
            }
            // ignore finished quests
            if (player.getCompletedQuests().contains(id)) {
                continue;
            }
            // ignore conditional quests
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            if (quest.canAcceptQuest(player)) {
                available.add(quest);
            }
        }

        return available;
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        // present a menu allowing to accept quests
        RPGCore.inst().getHUDManager().getQuestMenu().quests(this, player, npc);
    }
}