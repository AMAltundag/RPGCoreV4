package me.blutkrone.rpgcore.attribute;

import me.blutkrone.rpgcore.api.IContext;

/**
 * A numeric modifier that affects a certain attribute.
 */
public abstract class AttributeModifier implements IExpiringModifier {

    private final AttributeCollection collection;
    private boolean expired;

    /**
     * A modifier for a certain attribute.
     *
     * @param collection the holding container.
     */
    AttributeModifier(AttributeCollection collection) {
        this.collection = collection;
    }

    /**
     * Compute the value of the given modifier.
     *
     * @param context what context to be evaluated in.
     * @return the resulting modifier.
     */
    public abstract double evaluate(IContext context);

    @Override
    public void setExpired() {
        this.expired = true;
        this.collection.markDirty();
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    /**
     * A modifier which is constant after being created, being
     * the lightest on the processing.
     */
    public static final class Constant extends AttributeModifier {

        private final double factor;

        Constant(AttributeCollection collection, double factor) {
            super(collection);
            this.factor = factor;
        }

        @Override
        public double evaluate(IContext context) {
            return this.factor;
        }
    }

    /**
     * A modifier which is computed from one or two attributes, that
     * are relative to the invoking context.
     */
    public static final class Inherited extends AttributeModifier {

        private final String source;
        private final Object multiplier;

        /**
         * Compute the modifier based on one attribute.
         *
         * @param source     the attribute to read from the context
         * @param multiplier multiply the attribute with this factor
         */
        Inherited(AttributeCollection collection, String source, double multiplier) {
            super(collection);
            this.source = source;
            this.multiplier = multiplier;
        }

        /**
         * Compute the modifier based on two attributes.
         *
         * @param source     the attribute to read from the context
         * @param multiplier multiply the attribute with this factor
         */
        Inherited(AttributeCollection collection, String source, String multiplier) {
            super(collection);
            this.source = source;
            this.multiplier = multiplier;
        }

        @Override
        public double evaluate(IContext context) {
            double multiplier = (this.multiplier instanceof String)
                    ? context.evaluateAttribute(((String) this.multiplier))
                    : ((Double) this.multiplier);
            return context.evaluateAttribute(this.source) * multiplier;
        }
    }
}
