package me.blutkrone.rpgcore.editor;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.menu.EditorMenu;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A constraint limits the values which are accepted
 * into fields without an exact type.
 */
public interface IEditorConstraint {

    static Material[] UNIQUE_MATERIAL = new Material[] {
            Material.WHITE_WOOL,
            Material.ORANGE_WOOL,
            Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.PINK_WOOL,
            Material.GRAY_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.CYAN_WOOL,
            Material.PURPLE_WOOL,
            Material.BLUE_WOOL,
            Material.BROWN_WOOL,
            Material.GREEN_WOOL,
            Material.RED_WOOL,
            Material.BLACK_WOOL,
            Material.WHITE_TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.MAGENTA_TERRACOTTA,
            Material.LIGHT_BLUE_TERRACOTTA,
            Material.YELLOW_TERRACOTTA,
            Material.LIME_TERRACOTTA,
            Material.PINK_TERRACOTTA,
            Material.GRAY_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA,
            Material.CYAN_TERRACOTTA,
            Material.PURPLE_TERRACOTTA,
            Material.BLUE_TERRACOTTA,
            Material.BROWN_TERRACOTTA,
            Material.GREEN_TERRACOTTA,
            Material.RED_TERRACOTTA,
            Material.BLACK_TERRACOTTA,
            Material.WHITE_STAINED_GLASS,
            Material.ORANGE_STAINED_GLASS,
            Material.MAGENTA_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS,
            Material.LIME_STAINED_GLASS,
            Material.PINK_STAINED_GLASS,
            Material.GRAY_STAINED_GLASS,
            Material.LIGHT_GRAY_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS,
            Material.PURPLE_STAINED_GLASS,
            Material.BLUE_STAINED_GLASS,
            Material.BROWN_STAINED_GLASS,
            Material.GREEN_STAINED_GLASS,
            Material.RED_STAINED_GLASS,
            Material.BLACK_STAINED_GLASS,
            Material.WHITE_CONCRETE,
            Material.ORANGE_CONCRETE,
            Material.MAGENTA_CONCRETE,
            Material.LIGHT_BLUE_CONCRETE,
            Material.YELLOW_CONCRETE,
            Material.LIME_CONCRETE,
            Material.PINK_CONCRETE,
            Material.GRAY_CONCRETE,
            Material.LIGHT_GRAY_CONCRETE,
            Material.CYAN_CONCRETE,
            Material.PURPLE_CONCRETE,
            Material.BLUE_CONCRETE,
            Material.BROWN_CONCRETE,
            Material.GREEN_CONCRETE,
            Material.RED_CONCRETE,
            Material.BLACK_CONCRETE,
            Material.WHITE_CONCRETE_POWDER,
            Material.ORANGE_CONCRETE_POWDER,
            Material.MAGENTA_CONCRETE_POWDER,
            Material.LIGHT_BLUE_CONCRETE_POWDER,
            Material.YELLOW_CONCRETE_POWDER,
            Material.LIME_CONCRETE_POWDER,
            Material.PINK_CONCRETE_POWDER,
            Material.GRAY_CONCRETE_POWDER,
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.CYAN_CONCRETE_POWDER,
            Material.PURPLE_CONCRETE_POWDER,
            Material.BLUE_CONCRETE_POWDER,
            Material.BROWN_CONCRETE_POWDER,
            Material.GREEN_CONCRETE_POWDER,
            Material.RED_CONCRETE_POWDER,
            Material.BLACK_CONCRETE_POWDER
    };

    /**
     * A list view can be entered via a shift-left click, instead of typing
     * all elements are instead shown in a list.
     * <br>
     *
     * @return Itemized array of all options
     */
    default Map<String, ItemStack> getAllForList() {
        Map<String, ItemStack> items = new HashMap<>();
        getHint("").forEach((id) -> {
            items.put(id, ItemBuilder.of(getIconForList(id)).name("§f" + id).build());
        });
        return items;
    }

    /**
     * A list view can be entered via a shift-left click, instead of typing
     * all elements are instead shown in a list.
     * <br>
     * By default, this just hashes the ID and provides a random colored
     * block.
     *
     * @param id ID we want an icon for.
     * @return Itemized variant
     */
    default ItemStack getIconForList(String id) {
        return new ItemStack(UNIQUE_MATERIAL[Math.abs(id.hashCode()) % UNIQUE_MATERIAL.length]);
    }

    /**
     * If we are defined as a "mono type", we automate the type
     * selection and use 'mono_type' as our type.
     *
     * @return skip the type selection.
     */
    default boolean isMonoType() {
        return false;
    }

    /**
     * Get all constraints which match with the value.
     *
     * @param value the value to match with.
     * @return all constraints who match with the value.
     */
    List<String> getHint(String value);

    /**
     * Check if the given value is within the constraint.
     *
     * @param value the value to check for.
     * @return whether the value is in the constraint.
     */
    boolean isDefined(String value);

    /**
     * Extend the constraint by a given value, fail gracefully
     * should we be unable to extend.
     *
     * @param value which value to extend constraint by
     */
    void extend(String value);

    /**
     * Whether it is allowed to extend this constraint with
     * new values.
     *
     * @return true if we can extend the constraint.
     */
    boolean canExtend();

    /**
     * Get the constraint from the value at the given index.
     *
     * @param container which container to read from
     * @param index     the index we wish to inspect.
     * @return the constraint from the given index.
     */
    String getConstraintAt(List container, int index);

    /**
     * Present a menu which allows us to replace a value of the given list.
     *
     * @param container which container are we working with
     * @param index     the index we wish to inspect.
     * @param value     which value to substitute with.
     */
    void setElementAt(List container, int index, String value);

    /**
     * Present a menu which allows us to add a value to the given list.
     *
     * @param container which container are we working with
     * @param value     which value to append to the list
     */
    void addElement(List container, String value);

    /**
     * Transform the given value into the type backed by this constraint.
     *
     * @param value the string value which does match the constraint
     * @return the real type backing the string value
     */
    Object asTypeOf(String value);

    /**
     * We are given a
     * Transform a value into a string which can be casted back thorough
     * the {@link #asTypeOf(String)} method
     *
     * @param value the value to transform
     * @return the string equivalent of our value.
     */
    String toTypeOf(Object value);

    /**
     * Transform the given list into a preview.
     *
     * @param list the list to transform
     * @return the preview generated
     */
    List<String> getPreview(List<Object> list);

    /**
     * A preview text for a single object.
     *
     * @param object the object to preview.
     * @return value to be output
     */
    String getPreview(Object object);

    /**
     * Generic instructions on how to use this type of a constraint.
     *
     * @return instructions on this constraint.
     */
    List<String> getInstruction();

    /**
     * An attempt of focusing on an element, if the element doesn't
     * match any type we know we can return an error.
     *
     * @param editor  the editor we originate from
     * @param element the element we want to focus
     * @return true if we've focused
     */
    default boolean doListFocus(EditorMenu editor, Object element) {
        // identify which element we clicked on
        if (element instanceof IEditorBundle) {
            // update the focus to the said element
            editor.getFocus().setFocusToBundle(((IEditorBundle) element));
            // rebuild the editor with the updated focus
            editor.getMenu().queryRebuild();
            // operation is fine
            return true;
        } else {
            // cannot focus this type of element
            return false;
        }
    }
}
