package me.blutkrone.rpgcore.item.modifier;

import me.blutkrone.rpgcore.attribute.AttributeModifier;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.EditorAttributeAndFactor;
import me.blutkrone.rpgcore.hud.editor.root.EditorModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @see EditorModifier for the editor implementation
 */
public class CoreModifier {
    private final String id;

    private List<String> tags;
    private double weight;
    private double weight_per_quality;
    private Map<String, Double> attribute_effects;
    private ModifierType type;
    private boolean implicit;

    private ModifierStyle style;
    private String lc_readable;
    private String lc_category;

    /**
     * A container for information on how the given item
     * is engaging with the server.
     *
     */
    public CoreModifier(String id, EditorModifier editor) {
        this.id = id;
        this.tags = new ArrayList<>(editor.tags);
        this.weight = editor.weight;
        this.weight_per_quality = editor.weight_per_quality;
        this.attribute_effects = new HashMap<>();
        this.type = editor.type;
        this.implicit = editor.isImplicit();
        for (EditorAttributeAndFactor attribute : editor.attribute_effects) {
            this.attribute_effects.put(attribute.attribute, attribute.factor);
        }
        this.style = editor.readable_style;
        this.lc_readable = editor.lc_readable;
        this.lc_category = editor.lc_category;
    }

    /**
     * A distinct identifier of the item.
     *
     * @return identifier of this item.
     */
    public String getId() {
        return id;
    }

    /**
     * Calculate the weighting of this affix, relative to the quality
     * modifier that was input.
     *
     * @param quality pick more valuable modifiers
     * @return the weight of this modifier.
     */
    public double getWeight(double quality) {
        return this.weight + (this.weight_per_quality * quality);
    }

    /**
     * The tags present on this particular modifier.
     *
     * @return the tags present on the modifier.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Grant this modifier to the entity that requested it, do note
     * that the return value should be cleaned up appropriately else
     * it will accumulate bad data.
     *
     * @param entity which entity should receive the modifier.
     * @return the effect under which the item effect was received.
     */
    public List<AttributeModifier> apply(CoreEntity entity) {
        List<AttributeModifier> modifiers = new ArrayList<>();
        this.attribute_effects.forEach((attribute, factor) -> {
            modifiers.add(entity.getAttribute(attribute).create(factor));
        });
        return modifiers;
    }

    /**
     * Modifiers are either an implicit (all items got one) or an affix (is
     * rolled randomly on this item.)
     *
     * @return true if we are an implicit modifier.
     */
    public boolean isImplicit() {
        return implicit;
    }

    /**
     * How this modifier should be applied.
     *
     * @return how to apply the modifier.
     */
    public ModifierType getType() {
        return type;
    }

    /**
     * Retrieve the attribute effects.
     *
     * @return which attributes are available.
     */
    public Map<String, Double> getAttributeEffects() {
        return attribute_effects;
    }

    /**
     * The style in which we present the modifier.
     *
     * @return how to style this modifier.
     */
    public ModifierStyle getStyle() {
        return style;
    }

    /**
     * Language identifier containing readable modifier information.
     *
     * @return language identifier for the modifier.
     */
    public String getLCReadable() {
        return lc_readable;
    }

    /**
     * Category is only used for non-header styles, used to group
     * up the modifiers.
     *
     * @return the category to group under.
     */
    public String getLCCategory() {
        return lc_category;
    }
}
