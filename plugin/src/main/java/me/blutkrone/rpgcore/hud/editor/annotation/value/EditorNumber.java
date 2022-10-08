package me.blutkrone.rpgcore.hud.editor.annotation.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Treats the field as a plain number.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EditorNumber {

    /**
     * Name of the field being modified.
     *
     * @return a displayable name for the field.
     */
    String name();

    /**
     * Number cannot drop below minimum.
     *
     * @return lower bound
     */
    double minimum() default Integer.MIN_VALUE;

    /**
     * Number cannot exceed past maximum.
     *
     * @return upper bound.
     */
    double maximum() default Integer.MAX_VALUE;
}
