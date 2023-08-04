package me.blutkrone.rpgcore.attribute;

import me.blutkrone.rpgcore.api.IContext;

import java.util.ArrayList;
import java.util.List;

public class AttributeCollection {

    // the context that requested the collection
    private final IContext context;
    // modifiers created within the collection
    private final List<AttributeModifier.Constant> modifiers_constant = new ArrayList<>();
    private final List<AttributeModifier.Inherited> modifiers_inherited = new ArrayList<>();
    // if dirty, flush the constant modifiers
    private boolean dirty;
    // constant modifiers
    private double constant_cache;

    /**
     * A collection which holds a given
     *
     * @param context what context holds this collection
     */
    public AttributeCollection(IContext context) {
        this.context = context;
    }

    /**
     * This method is intended for internal usage, only used for the
     * sake of providing access to certain numbers into systems that
     * read from attributes.
     *
     * @param override the modifier to override into.
     */
    public void setOverride(double override) {
        this.modifiers_constant.forEach(AttributeModifier::setExpired);
        this.modifiers_inherited.forEach(AttributeModifier::setExpired);
        this.create(override);
    }

    /**
     * Compute the value of this collection.
     *
     * @return the resulting value
     */
    public double evaluate() {
        if (dirty) {
            // get rid of no longer active modifiers
            this.modifiers_constant.removeIf(AttributeModifier::isExpired);
            this.modifiers_inherited.removeIf(AttributeModifier::isExpired);
            // re-sum the constant modifiers
            this.constant_cache = 0d;
            for (AttributeModifier.Constant constant : modifiers_constant) {
                this.constant_cache += constant.evaluate(getContext());
            }
        }
        // sum the constant with the inheritance
        double result = this.constant_cache;
        for (AttributeModifier.Inherited inherited : this.modifiers_inherited) {
            result += inherited.evaluate(getContext());
        }
        // offer up the resulting modifier
        return result;
    }

    /**
     * Create an attribute modifier with a constant factor.
     *
     * @param factor the value of the modifier
     * @return modifier instance which can be invalidated
     */
    public AttributeModifier create(double factor) {
        markDirty();
        AttributeModifier.Constant modifier = new AttributeModifier.Constant(this, factor);
        modifiers_constant.add(modifier);
        return modifier;
    }

    /**
     * Create an attribute modifier with a constant factor.
     *
     * @param factor    the value of the modifier
     * @param magnitude a multiplier differing by attribute type
     * @return modifier instance which can be invalidated
     */
    public AttributeModifier create(String factor, double magnitude) {
        markDirty();
        AttributeModifier.Inherited modifier = new AttributeModifier.Inherited(this, factor, magnitude);
        modifiers_inherited.add(modifier);
        return modifier;
    }

    /**
     * Create an attribute modifier with a constant factor.
     *
     * @param factor    the value of the modifier
     * @param magnitude a multiplier differing by attribute type
     * @return modifier instance which can be invalidated
     */
    public AttributeModifier create(String factor, String magnitude) {
        markDirty();
        AttributeModifier.Inherited modifier = new AttributeModifier.Inherited(this, factor, magnitude);
        modifiers_inherited.add(modifier);
        return modifier;
    }

    /**
     * Mark this collection as dirty, forcing it to re-compute.
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * The context which holds this collection.
     *
     * @return the context which holds this collection
     */
    public IContext getContext() {
        return context;
    }
}
