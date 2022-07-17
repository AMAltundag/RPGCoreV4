package me.blutkrone.rpgcore.hud.editor;

import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;

import java.util.List;

/**
 * A constraint limits the values which are accepted
 * into fields without an exact type.
 */
public interface IEditorConstraint {

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
     * Delegate the request to edit a value, this is primarily intended for
     * something which is indirectly linked via an ID.
     *
     * @param source
     * @param value
     */
    default void editDelegate(IChestMenu source, Object value) {
        source.getViewer().sendMessage("§cThis type of value cannot be edited!");
    }
}
