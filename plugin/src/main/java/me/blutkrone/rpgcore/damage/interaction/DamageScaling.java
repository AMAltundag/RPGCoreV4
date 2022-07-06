package me.blutkrone.rpgcore.damage.interaction;

import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;

import java.util.HashSet;
import java.util.Set;

/**
 * A rule to scale damage thorough
 */
public class DamageScaling {
    // which elements are affected by the scaling
    private final Set<DamageElement> elements;
    // the attribute we read from
    private final String entity_attribute;
    // the effectiveness to scale it with
    private final double effectiveness;

    public DamageScaling(Set<DamageElement> elements, String entity_attribute, double effectiveness) {
        this.elements = elements;
        this.entity_attribute = entity_attribute;
        this.effectiveness = effectiveness;
    }

    /**
     * A rule to scale damage thorough
     *
     * @param config the config to read from
     */
    public DamageScaling(IDamageManager manager, ConfigWrapper config) {
        this.elements = new HashSet<>(manager.getElements(config.getStringList("elements")));
        this.entity_attribute = config.getString("read-attribute", "NOTHINGNESS");
        this.effectiveness = config.getDouble("effectiveness");
    }

    /**
     * Which elements are scaled by this rule.
     *
     * @return the elements to scale by this rule.
     */
    public Set<DamageElement> getElements() {
        return elements;
    }

    /**
     * Attribute which we read from, any attribute works fine
     * here.
     *
     * @return attribute which is read from
     */
    public String getEntityAttribute() {
        return entity_attribute;
    }

    /**
     * Multiplies the attribute with the effectiveness before it
     * will be allowed to contribute.
     *
     * @return the effectiveness of the read attribute
     */
    public double getEffectiveness() {
        return effectiveness;
    }
}
