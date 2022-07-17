package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.hud.editor.root.npc.EditorQuestTrait;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A trait intended for progression in a quest, this will
 * supersede any other trait depending on status
 * <p>
 * <ul>
 * <li>Automatically checks for quest state</li>
 * <li>Can present player with dialogue</li>
 * <li>Can present player with rewards</li>
 * <li>Can offer "take item from player" menu</li>
 * <li>Can offer "make a quest choice" menu</li>
 * <li>Quest info added to cortex menu</li>
 * </ul>
 */
public class CoreQuestTrait extends AbstractCoreTrait {

    public CoreQuestTrait(EditorQuestTrait editor) {
        super(editor);
    }

    /**
     * Check if a quest trait is available.
     *
     * @return true if a quest progression is available.
     */
    public boolean isAvailable() {
        Bukkit.getLogger().severe("not implemented (quest trait check)");
        return false;
    }

    @Override
    public void engage(Player player) {

    }
}