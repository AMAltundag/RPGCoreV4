package me.blutkrone.rpgcore.editor.bundle;

import me.blutkrone.rpgcore.editor.annotation.EditorName;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A bundle serves to contain multiple objects, including other
 * bundles.
 * <br>
 * A zero-parameter constructor is to be supplied by any bundle.
 *
 * @see EditorName to ease user experience
 * @see EditorTooltip to ease user experience
 */
public interface IEditorBundle {

    /**
     * Create an itemized preview of this bundle.
     *
     * @return who created a preview.
     */
    ItemStack getPreview();

    /**
     * A name for this bundle.
     *
     * @return the name for this bundle.
     */
    String getName();

    /**
     * Instructions on how to use this bundle.
     *
     * @return instructions on this bundle.
     */
    List<String> getInstruction();
}
