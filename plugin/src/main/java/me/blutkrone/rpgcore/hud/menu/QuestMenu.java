package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.impl.CoreQuestTrait;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskDeliver;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuestMenu {

    public QuestMenu() {
    }

    /**
     * A menu allowing the player to check the quests
     * given by this NPC.
     *
     * @param trait  the trait which tracks quests
     * @param player whose quest state to check
     * @param npc    when closing open the cortex
     */
    public void quests(CoreQuestTrait trait, Player player, CoreNPC npc) {
        // only show unclaimed but available quests
        List<String> quests = new ArrayList<>();
        CorePlayer _core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        for (String id : trait.getQuests()) {
            if (!_core_player.getActiveQuestIds().contains(id)) {
                CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
                if (quest.canAcceptQuest(_core_player)) {
                    quests.add(id);
                }
            }
        }

        // present a menu to interact
        new me.blutkrone.rpgcore.menu.QuestMenu.Quest(quests, npc).finish(player);
    }

    /**
     * Present reward claiming menu to a player.
     *
     * @param quest  which quest do we offer rewards for
     * @param player who will get the rewards
     * @param npc    when closing open the cortex
     */
    public void reward(CoreQuest quest, Player player, CoreNPC npc) {
        // show menu to pick a reward from a quest
        new me.blutkrone.rpgcore.menu.QuestMenu.Reward(quest, npc).finish(player);
    }

    /**
     * Show a menu allowing to deliver requested items.
     *
     * @param task   the task for delivery
     * @param player whose quest state to check
     * @param npc    when closing open the cortex
     */
    public void delivery(CoreQuestTaskDeliver task, Player player, CoreNPC npc) {
        new me.blutkrone.rpgcore.menu.QuestMenu.Delivery(task, npc).finish(player);
    }
}
