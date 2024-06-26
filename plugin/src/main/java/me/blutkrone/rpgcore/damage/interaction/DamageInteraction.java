package me.blutkrone.rpgcore.damage.interaction;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.attribute.AttributeCollection;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import org.bukkit.entity.LivingEntity;

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
    // additional modifiers available here
    private Map<String, AttributeCollection> attribute = new HashMap<>();
    // a context may be provided optionally
    private IContext source_context = null;
    // blame generated for the skill
    private String damage_blame = "unknown";
    // multiplier to the damage inflicted
    private double multiplier = 1d;
    // knockback power applying to damage
    private double knockback_power;

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
     * The intensity of the knockback, note that no knockback is
     * to be applied if we are damage over time.
     *
     * @return Knockback power.
     */
    public double getKnockback() {
        return knockback_power;
    }

    /**
     * The intensity of the knockback, note that no knockback is
     * to be applied if we are damage over time.
     *
     * @param knockback_power Updated knockback
     */
    public void setKnockback(double knockback_power) {
        this.knockback_power = knockback_power;
    }

    /**
     * Identifies who is to be blamed for the damage, this is
     * meant for something like a damage log.
     *
     * @return who is to blame for damage.
     */
    public String getDamageBlame() {
        return damage_blame;
    }

    /**
     * Identifies who is to be blamed for the damage, this is
     * meant for something like a damage log.
     *
     * @param damage_blame who should bear blame.
     */
    public void setDamageBlame(String damage_blame) {
        this.damage_blame = damage_blame;
    }

    /**
     * A source context is provided if the damage was generated thorough the
     * skill system. Otherwise this is empty.
     *
     * @return the source context, if it exists.
     */
    public Optional<IContext> getSourceContext() {
        return Optional.ofNullable(source_context);
    }

    /**
     * Binds the instance to a context, this is meant for skills to identify
     * damage from other skills. Should always be a skill context.
     *
     * @param source_context the context.
     */
    public void setSourceContext(IContext source_context) {
        this.source_context = source_context;
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
     * Get the total sum of damage.
     *
     * @return total damage dealt.
     */
    public double getDamage() {
        double total = 0d;
        for (double damage : this.damage.values()) {
            total += damage;
        }
        return total;
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
     * Acquire a subset of tags to further identify what sort of
     * damage we are dealing.
     *
     * @param tags the tags to acquire
     */
    public void addTags(Collection<String> tags) {
        this.tags.addAll(tags);
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
        return this.tags.contains(tag.toLowerCase()) || (context != null && context.checkForTag(tag.toLowerCase()));
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
        return local + entity.getAttribute(attribute).evaluate();
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
        return this.attribute.computeIfAbsent(attribute.toLowerCase(), (k -> new AttributeCollection(IContext.EMPTY)));
    }

    /**
     * Meant for death handling to transfer rage to someone else.
     */
    public void shiftRageBlame() {
        // only mobs do have rage
        if (!(attacker instanceof CoreMob)) {
            return;
        }
        // only transfer rage if we actually hold it
        IEntityBase attacker_base = ((CoreMob) attacker).getBase();
        LivingEntity rage_entity = attacker_base.getRageEntity();
        if (rage_entity == null || !defender.getUniqueId().equals(rage_entity.getUniqueId())) {
            return;
        }
        // find someone to shift the rage to
        List<CoreEntity> candidates = attacker.getNearby(32d);
        candidates.remove(attacker);
        candidates.removeIf(e -> e.distance(attacker) > 32d || e.isFriendly(attacker) || !e.hasLineOfSight(attacker));
        CoreEntity picked = null;
        double closest = Double.MAX_VALUE;
        for (CoreEntity candidate : candidates) {
            double dist = candidate.distance(attacker);
            if (dist < closest || picked == null) {
                picked = candidate;
                closest = dist;
            }
        }
        // if we can, transfer otherwise reset
        if (picked != null) {
            double focus = picked.evaluateAttribute("RAGE_FOCUS");
            attacker_base.rageTransfer(picked.getEntity(), focus);
        } else {
            attacker_base.resetRage();
        }
    }

    /**
     * Enforce a certain multiplier to the damage that will be applied to the
     * final amount of damage.
     *
     * @param multiplier Multiplier to damage
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Enforce a certain multiplier to the damage that will be applied to the
     * final amount of damage.
     *
     * @return Multiplier to damage
     */
    public double getMultiplier() {
        return multiplier;
    }
}
