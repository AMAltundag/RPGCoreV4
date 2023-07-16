package me.blutkrone.rpgcore.editor.root;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.menu.EditorMenu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface IEditorRoot<K> extends IEditorBundle {

    /**
     * Retrieve the file we are backed up by.
     *
     * @return the file we are backed by.
     */
    File getFile();

    /**
     * Update the file which handles de/serialization of this.
     *
     * @param file which file to de/serialize from
     */
    void setFile(File file);

    /**
     * Dump the current state into the file we are linked to.
     */
    void save() throws IOException;

    /**
     * Transform this configuration into a runtime instance, do note
     * that this should be a one-directional process.
     *
     * @param id the ID of the runtime instance.
     * @return the baked runtime instance
     */
    K build(String id);

    /**
     * Custom control for specialized editing interfaces.
     *
     * @return List of control items
     */
    default List<ItemStack> getCustomControls() {
        return new ArrayList<>();
    }

    /**
     * Custom control for specialized editing interfaces.
     *
     * @param menu  Which menu was interacted.
     * @param item  What custom control was clicked.
     * @param click
     * @return Evaluated custom interaction.
     */
    default boolean onCustomControl(EditorMenu menu, ItemStack item, ClickType click) {
        return false;
    }
}