package me.blutkrone.rpgcore.language.pattern;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.language.NumberFormat;

public abstract class AttributePattern {

    /**
     * Merge this pattern into a string, appending behind it.
     *
     * @param builder where we are expected to dump our data
     * @param context a context dedicated to skill numbers
     */
    public abstract void mergeTo(StringBuilder builder, IContext context);

    public static final class Constant extends AttributePattern {

        private final String component;

        public Constant(String component) {
            this.component = component;
        }

        @Override
        public void mergeTo(StringBuilder builder, IContext context) {
            builder.append(this.component);
        }
    }

    public static final class Variable extends AttributePattern {
        // which attribute do we display
        private final String attribute;
        // how do we format the attribute
        private NumberFormat format;
        // what precision do we apply to the format
        private int precision;

        public Variable(String component) {
            String[] parameters = component.split(":");
            this.attribute = parameters[0];
            this.format = parameters.length < 2 ? NumberFormat.DECIMAL : NumberFormat.valueOf(parameters[1]);
            this.precision = parameters.length < 3 ? 0 : Integer.parseInt(parameters[2]);
        }

        @Override
        public void mergeTo(StringBuilder builder, IContext context) {
            try {
                double factor = context.evaluateAttribute(this.attribute);
                builder.append(this.format.translate(factor, this.precision));
            } catch (Exception e) {
                builder.append("BAD_RESULT");
            }
        }
    }
}
