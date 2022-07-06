package me.blutkrone.rpgcore.hud.editor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Option to attach a name wherever a generic editor
 * annotation is not available.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface EditorName {
    /**
     * Option to attach a name wherever a generic editor
     * annotation is not available.
     *
     * @return name for the object.
     */
    String name();
}
