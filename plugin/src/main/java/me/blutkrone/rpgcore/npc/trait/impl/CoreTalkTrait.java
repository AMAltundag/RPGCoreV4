package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.npc.EditorTalkTrait;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

/**
 * Sends a message if player is very close by.
 * <br>
 * Provides no entry to the cortex.
 */
public class CoreTalkTrait extends AbstractCoreTrait {
    public CoreTalkTrait(EditorTalkTrait editor) {
        super(editor);

        RPGCore.inst().getLogger().info("not implemented (talk trait)");
    }

    @Override
    public void engage(Player player, CoreNPC npc) {

    }
}
