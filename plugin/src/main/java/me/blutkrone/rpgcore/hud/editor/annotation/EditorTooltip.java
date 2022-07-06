package me.blutkrone.rpgcore.hud.editor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Additional information about the field being modified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface EditorTooltip {
    /**
     * Additional information about the field being modified.
     *
     * @return additional information.
     */
    String[] tooltip();
}
