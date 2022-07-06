package me.blutkrone.rpgcore.language.pattern;

import me.blutkrone.rpgcore.language.NumberFormat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public abstract class NumberPattern {

    /**
     * Merge this pattern into a string, appending behind it.
     *
     * @param builder where we are expected to dump our data
     * @param factor  the number which we want to format
     */
    public abstract void mergeTo(StringBuilder builder, double factor);

    public static final class Constant extends NumberPattern {

        private final String component;

        public Constant(String component) {
            this.component = component;
        }

        @Override
        public void mergeTo(StringBuilder builder, double factor) {
            builder.append(this.component);
        }
    }

    public static final class Variable extends NumberPattern {

        private NumberFormat format;
        private int precision;

        public Variable(String component) {
            Queue<String> args = new LinkedList<>(Arrays.asList(component.split("\\:")));
            this.format = args.isEmpty() ? NumberFormat.DECIMAL : NumberFormat.valueOf(args.poll());
            this.precision = args.isEmpty() ? 0 : Integer.parseInt(args.poll());
        }

        @Override
        public void mergeTo(StringBuilder builder, double factor) {
            try {
                builder.append(this.format.translate(factor, this.precision));
            } catch (Exception e) {
                builder.append("BAD_RESULT");
            }
        }
    }
}
