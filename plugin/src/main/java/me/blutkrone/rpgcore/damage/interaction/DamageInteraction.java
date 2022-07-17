package me.blutkrone.rpgcore.damage.interaction;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.attribute.AttributeCollection;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;

import java.util.*;

/**
 * An interaction of damage which is to be processed by the
 * given damage manager implementation.
 */
public final class DamageInteraction {

    // the way this damage should be computed
    private IDamageType type;
    // who deals the damage
    private CoreEntity attacker;
    // who takes the damage
    private CoreEntity defender;
    // base damage to be inflicted
    private Map<DamageElement, Double> damage = new HashMap<>();
    // tags to identify this damage type
    private Set<String> tags = new HashSet<>();
    // scaling rules to apply
    private List<DamageScaling> scaling_attacker_multi = new ArrayList<>();
    private List<DamageScaling> scaling_attacker_flat = new ArrayList<>();
    private List<DamageScaling> scaling_attacker_crit_chance = new ArrayList<>();
    private List<DamageScaling> scaling_attacker_crit_damage = new ArrayList<>();
    // additional modifiers available here
    private Map<String, AttributeCollection> attribute = new HashMap<>();

    /**
     * An interaction holding information about damage.
     *
     * @param type     how to process the damage
     * @param defender who takes the damage
     * @param attacker WHO deals the damage
     */
    public DamageInteraction(IDamageType type, CoreEntity defender, CoreEntity attacker) {
        if (defender == null) throw new NullPointerException("defender cannot be null!");
        if (type == null) throw new NullPointerException("type cannot be null!");
        this.type = type;
        this.defender = defender;
        this.attacker = attacker;
    }

    /**
     * Who is credited with dealing this damage.
     *
     * @return the entity who inflicted the damage.
     */
    public CoreEntity getAttacker() {
        return attacker;
    }

    /**
     * Who will eventually take the damage.
     *
     * @return the entity who suffered the damage.
     */
    public CoreEntity getDefender() {
        return defender;
    }

    /**
     * The type of damage specifies how it is to processed, there will
     * always be a type to the damage dealt.
     *
     * @return what type of damage is dealt
     */
    public IDamageType getType() {
        return type;
    }

    /**
     * Retrieve the damage dealt by the given element.
     *
     * @param element which element to process
     * @return the base damage to be dealt
     */
    public double getDamage(DamageElement element) {
        return this.damage.getOrDefault(element, 0d);
    }

    /**
     * Update the damage to be dealt of a given element.
     *
     * @param element which element to process
     * @param damage  updated damage value
     */
    public void setDamage(DamageElement element, double damage) {
        this.damage.put(element, damage);
    }

    /**
     * Acquire a subset of tags to further identify what sort of
     * damage we are dealing.
     *
     * @param tags the tags to acquire
     */
    public void addTags(String... tags) {
        this.tags.addAll(Arrays.asList(tags));
    }

    /**
     * Check if a certain tag is present, if it isn't present on
     * this interaction it may be present on the related context
     *
     * @param tag     what tag to check for
     * @param context additional context which can provide it
     * @return true if we have the tag
     */
    public boolean checkForTag(String tag, CoreEntity context) {
        return this.tags.contains(tag) || (context != null && context.checkForTag(tag));
    }

    /**
     * Evaluate an attribute, in conjunction with a context that also
     * is able to influence it.
     *
     * @param attribute which attribute to evaluate
     * @param entity    whose to base the evaluation off
     * @return the resulting modifier
     */
    public double evaluateAttribute(String attribute, CoreEntity entity) {
        // only sum with entity attribute if necessary
        if (entity == null)
            throw new NullPointerException("Cannot evaluate attribute from null");
        // compute the local value of the modifier
        double local = getAttribute(attribute).evaluate();
        // join with shared value of the modifier
        return local + entity.evaluateAttribute(attribute);
    }

    /**
     * The tags which allow further identification on this damage
     * instance. When checking for a tag
     *
     * @return tagging present on this interaction.
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * The collection which contains attributes handled by this
     * damage instance, do note that the modifiers do not belong
     * to the attacker or defender.
     *
     * @param attribute the attribute to retrieve
     * @return local attribute modifier
     */
    public AttributeCollection getAttribute(String attribute) {
        return this.attribute.computeIfAbsent(attribute, (k -> new AttributeCollection(IContext.EMPTY)));
    }

}