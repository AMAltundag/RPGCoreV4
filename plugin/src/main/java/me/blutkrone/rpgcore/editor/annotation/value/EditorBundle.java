package me.blutkrone.rpgcore.editor.annotation.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Treat a field as an complex object.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EditorBundle {
    /**
     * Name of the field being modified.
     *
     * @return a displayable name for the field.
     */
    String name();
}