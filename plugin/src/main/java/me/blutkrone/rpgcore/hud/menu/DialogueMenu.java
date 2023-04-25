package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskTalk;
import org.bukkit.entity.Player;

public class DialogueMenu {


    public DialogueMenu() {
    }

    /**
     * Present dialogue to a player.
     *
     * @param _dialogue which dialogue are we presenting
     * @param _player   who will see the dialogue
     * @param npc       who to source the dialogue from
     * @param task      an optional task we sourced the dialogue from
     */
    public void open(CoreDialogue _dialogue, Player _player, CoreNPC npc, CoreQuestTaskTalk task) {
        new me.blutkrone.rpgcore.menu.DialogueMenu(_dialogue, npc, task).finish(_player);
    }
}
