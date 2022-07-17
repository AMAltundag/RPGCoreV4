package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.hud.editor.root.npc.EditorGateTrait;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

/**
 * Grants access to dungeon portals/matchmaker
 */
public class CoreGateTrait extends AbstractCoreTrait {
    public CoreGateTrait(EditorGateTrait editor) {
        super(editor);
    }

    @Override
    public void engage(Player player) {

    }
}
