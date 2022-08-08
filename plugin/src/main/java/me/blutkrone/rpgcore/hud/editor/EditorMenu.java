package me.blutkrone.rpgcore.hud.editor;

import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import org.bukkit.entity.Player;

/**
 * Administrative menu to edit relevant components of the
 * core.
 */
public class EditorMenu {

    /**
     * Create and open an editor for the given index, the said
     * editor is allowed unrestricted access on the index.
     *
     * @param player who will receive the editor
     * @param index  the index we wish to edit.
     */
    public void edit(Player player, EditorIndex index) {
        new me.blutkrone.rpgcore.menu.EditorMenu(index).finish(player);
    }
}