package me.blutkrone.rpgcore.hud.editor.design.designs;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.menu.EditorMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IDesignFieldEditor {

    /**
     * Present the editor to manipulate a field of the bundle, to
     * the requesting viewer.
     *
     * @param bundle which bundle to edit
     * @param viewer who will do the editing
     * @param editor the editor menu that opens this field editor
     */
    void edit(IEditorBundle bundle, Player viewer, EditorMenu editor);

    /**
     * Fetch the name of this design element.
     *
     * @return the name we
     */
    String getName();

    /**
     * Fetch an info string about the bundle.
     *
     * @param bundle the bundle we've fetched.
     * @return a readable info of the value of the given bundle.
     */
    String getInfo(IEditorBundle bundle) throws Exception;

    /**
     * The icon that provides information about the field.
     *
     * @return an icon to represent the bundle by this element.
     */
    ItemStack getIcon(IEditorBundle bundle) throws Exception;
}
