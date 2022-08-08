package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.hud.editor.bundle.npc.EditorMailTrait;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

/**
 * Messages with items attached to them.
 * <p>
 * Mail is to be shared across the roster.
 */
public class CoreMailTrait extends AbstractCoreTrait {
    public CoreMailTrait(EditorMailTrait editor) {
        super(editor);
    }

    @Override
    public void engage(Player player, CoreNPC npc) {

    }
}
