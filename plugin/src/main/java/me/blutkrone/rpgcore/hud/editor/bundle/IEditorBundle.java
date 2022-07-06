package me.blutkrone.rpgcore.hud.editor.bundle;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A bundle serves to contain multiple objects, including other
 * bundles.
 * <p>
 * A zero-parameter constructor is to be supplied by any bundle.
 *
 * @see me.blutkrone.rpgcore.hud.editor.annotation.EditorName to ease user experience
 * @see me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip to ease user experience
 */
public interface IEditorBundle {

    /**
     * Validate if this object meets conditions necessary
     * for continued existence.
     *
     * @return true if this object is completed.
     */
    boolean isValid();

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
