package me.blutkrone.rpgcore.hud.editor.annotation.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Treat a field as an RGB integer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EditorColor {
    /**
     * Name of the field being modified.
     *
     * @return a displayable name for the field.
     */
    String name();
}
