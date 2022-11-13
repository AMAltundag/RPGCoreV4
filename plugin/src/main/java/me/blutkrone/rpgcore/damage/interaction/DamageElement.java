package me.blutkrone.rpgcore.damage.interaction;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;

import java.util.List;

/**
 * The element used in a damage interaction.
 */
public class DamageElement {

    // identifier of this element
    private final String id;

    // % based damage reduction
    private final List<String> resistance;
    // counter to resistance
    private final List<String> penetration;
    // ceiling of damage reduction
    private final String max_reduction;
    // multipliers to the damage inflicted
    private final List<String> multiplier;
    // all damage taken as this element
    private final String taken;
    // damage received from defender
    private final List<String> received;
    // range of damage, and chance to roll again
    private final List<String> minimum_range;
    private final List<String> maximum_range;
    private final List<String> range_lucky;
    private final List<String> range_unlucky;
    // % of non-element gained as this element
    private final List<String> extra;

    /**
     * The element used in a damage interaction.
     *
     * @param id     identifier of the element
     * @param config the config to read from
     */
    public DamageElement(String id, ConfigWrapper config) {
        this.id = id;

        this.resistance = config.getStringList("resistance");
        this.penetration = config.getStringList("penetration");
        this.max_reduction = config.getString("max-reduction", "NOTHINGNESS");
        this.multiplier = config.getStringList("multiplier");
        this.taken = config.getString("taken", "NOTHINGNESS");
        this.extra = config.getStringList("extra");
        this.received = config.getStringList("received");
        this.minimum_range = config.getStringList("minimum");
        this.maximum_range = config.getStringList("maximum");
        this.range_lucky = config.getStringList("lucky");
        this.range_unlucky = config.getStringList("unlucky");
    }

    /**
     * Identifier for this specific element.
     *
     * @return the identifier of this element
     */
    public String getId() {
        return id;
    }

    /**
     * Read from the defender
     * <p>
     * Summed with penetration
     * <p>
     * Multiplier group A
     *
     * @return offsets penetration
     */
    public List<String> getResistanceAttribute() {
        return resistance;
    }

    /**
     * Read from the attacker
     * <p>
     * Summed with resistance
     * <p>
     * Multiplier group A
     *
     * @return offsets resistance
     */
    public List<String> getPenetrationAttribute() {
        return penetration;
    }

    /**
     * Read from the defender
     * <p>
     * Caps maximum resistance
     * <p>
     * No lower limit applied
     *
     * @return upper limit on resistance
     */
    public String getMaxReductionAttribute() {
        return max_reduction;
    }

    /**
     * Read from the attacker
     * <p>
     * Multiplier group B
     *
     * @return damage multiplier
     */
    public List<String> getMultiplierAttribute() {
        return multiplier;
    }

    /**
     * Read from defender
     * <p>
     * Transforms any damage taken to this element
     * <p>
     * Applies during resistance calculation
     *
     * @return other elements taken as this element
     */
    public String getTakenAttribute() {
        return taken;
    }

    /**
     * Read from attacker
     * <p>
     * Damage not of this element is gained as extra damage
     * <p>
     * Original element is not reduced
     * <p>
     * Only affects multipliers and flat damage
     *
     * @return extra damage based on other elements
     */
    public List<String> getExtraAttribute() {
        return extra;
    }

    /**
     * Read from defender
     * <p>
     * Multiplier group C
     *
     * @return multiplies damage taken
     */
    public List<String> getReceived() {
        return received;
    }

    /**
     * Read from attacker
     * <p>
     * Modifier that lowers the range of maximum damage.
     *
     * @return % that lowers range of damage dealt
     */
    public List<String> getMinimumRange() {
        return minimum_range;
    }

    /**
     * Read from attacker
     * <p>
     * Modifier that raises the range of maximum damage.
     *
     * @return % that raises range of damage dealt
     */
    public List<String> getMaximumRange() {
        return maximum_range;
    }

    /**
     * Read from attacker
     * <p>
     * Roll damage range twice, pick better.
     *
     * @return chance to pick better damage roll.
     */
    public List<String> getRangeLucky() {
        return range_lucky;
    }

    /**
     * Read from attacker
     * <p>
     * Roll damage range twice, pick worse.
     *
     * @return chance to pick worse damage roll.
     */
    public List<String> getRangeUnlucky() {
        return range_unlucky;
    }
}