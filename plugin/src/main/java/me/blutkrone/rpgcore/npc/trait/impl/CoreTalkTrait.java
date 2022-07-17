package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.hud.editor.root.npc.EditorTalkTrait;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

/**
 * Sends a message if player is very close by.
 * <p>
 * Provides no entry to the cortex.
 */
public class CoreTalkTrait extends AbstractCoreTrait {
    public CoreTalkTrait(EditorTalkTrait editor) {
        super(editor);
    }

    @Override
    public void engage(Player player) {

    }
}
