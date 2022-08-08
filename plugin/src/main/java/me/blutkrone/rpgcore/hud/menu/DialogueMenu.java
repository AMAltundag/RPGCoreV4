package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import org.bukkit.entity.Player;

public class DialogueMenu {


    public DialogueMenu() {
    }

    /**
     * Present dialogue to a player.
     *
     * @param _dialogue  which dialogue are we presenting
     * @param _player    who will see the dialogue
     * @param completion will grant quest completion
     */
    public void open(CoreDialogue _dialogue, Player _player, String completion) {
        new me.blutkrone.rpgcore.menu.DialogueMenu(_dialogue, completion).finish(_player);
    }
}
