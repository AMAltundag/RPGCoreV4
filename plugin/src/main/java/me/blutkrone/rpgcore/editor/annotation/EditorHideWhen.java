package me.blutkrone.rpgcore.editor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field is hidden when condition is met, if available will also
 * check the HideWhen of the inherited field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EditorHideWhen {
    /**
     * Which field is checked to determine visibility.
     *
     * @return the field to be checked.
     */
    String field();

    /**
     * Value of field is turned to a string, and then compared
     * against this. Another comparision is done based with the
     * String wrapped {@link Object#getClass()}
     *
     * @return the value of the field.
     */
    String[] value();

    /**
     * Invert the check on value.
     *
     * @return true if we should invert the check.
     */
    boolean invert() default false;
}
