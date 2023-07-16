package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.editor.bundle.npc.EditorEssenceTrait;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

/**
 * Crafting menu for affix based items.
 */
public class CoreEssenceTrait extends AbstractCoreTrait {
    public CoreEssenceTrait(EditorEssenceTrait editor) {
        super(editor);
    }

    @Override
    public void engage(Player player, CoreNPC npc) {

    }
}
