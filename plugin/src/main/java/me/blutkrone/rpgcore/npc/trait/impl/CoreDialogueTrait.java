package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorDialogueTrait;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import org.bukkit.entity.Player;

/**
 * Presents a dialogue chain to a player, this is NOT
 * dialogue directly associated with a quest.
 */
public class CoreDialogueTrait extends AbstractCoreTrait {

    private CoreDialogue dialogue;

    public CoreDialogueTrait(EditorDialogueTrait editor) {
        super(editor);

        if (!editor.dialogue.isEmpty()) {
            this.dialogue = RPGCore.inst().getQuestManager().getIndexDialogue().get(editor.dialogue.get(0));
        }
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        if (this.dialogue != null) {
            RPGCore.inst().getHUDManager().getDialogueMenu().open(this.dialogue, player, null);
        }
    }
}
