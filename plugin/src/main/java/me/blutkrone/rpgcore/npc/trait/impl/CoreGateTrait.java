package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.editor.bundle.npc.EditorGateTrait;
import me.blutkrone.rpgcore.npc.CoreNPC;
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
    public void engage(Player player, CoreNPC npc) {

    }
}
