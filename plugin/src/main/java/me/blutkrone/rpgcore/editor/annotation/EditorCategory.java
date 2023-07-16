package me.blutkrone.rpgcore.editor.annotation;

import org.bukkit.Material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An organisation annotation, all following editor annotations
 * are to going to be categorized underneath it.
 * <p>
 * The object to bundle must not necessarily be editable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface EditorCategory {
    /**
     * The first line should be as brief as possible, since
     * it will be trimmed if too long.
     *
     * @return an info array about the bundle field.
     */
    String[] info();

    /**
     * An icon string written like "material:model" to
     * contain the information under itself.
     *
     * @return a "material:model" string.
     */
    Material icon();
}
