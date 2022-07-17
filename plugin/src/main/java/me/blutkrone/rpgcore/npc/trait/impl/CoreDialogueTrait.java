package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.hud.editor.root.npc.EditorDialogueTrait;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

/**
 * Presents a dialogue chain to a player, this is NOT
 * dialogue directly associated with a quest.
 */
public class CoreDialogueTrait extends AbstractCoreTrait {
    public CoreDialogueTrait(EditorDialogueTrait editor) {
        super(editor);
    }

    @Override
    public void engage(Player player) {

    }
}
