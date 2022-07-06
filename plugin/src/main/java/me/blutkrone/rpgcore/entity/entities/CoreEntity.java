package me.blutkrone.rpgcore.entity.entities;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.activity.IActivity;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.attribute.AttributeCollection;
import me.blutkrone.rpgcore.attribute.IExpiringModifier;
import me.blutkrone.rpgcore.attribute.TagModifier;
import me.blutkrone.rpgcore.damage.ailment.AbstractAilment;
import me.blutkrone.rpgcore.damage.ailment.AilmentTracker;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.EntityManager;
import me.blutkrone.rpgcore.entity.resource.EntityResource;
import me.blutkrone.rpgcore.entity.resource.EntityWard;
import me.blutkrone.rpgcore.entity.tasks.BukkitImmolationTask;
import me.blutkrone.rpgcore.entity.tasks.CoreToBukkitAttributeTask;
import me.blutkrone.rpgcore.entity.tasks.EntityActivityTask;
import me.blutkrone.rpgcore.entity.tasks.EntityEffectTask;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * The root for every entity affiliated with the core, primarily intended
 * for "living" entities.
 * If a bukkit entity has no associated core entity it will not be affected
 * by the core in any form.
 */
public class CoreEntity implements IContext, IOrigin {

    protected final List<BukkitTask> bukkit_tasks;
    // bukkit specific entity logic
    private final UUID bukkit_uuid;
    // a "provider" which supplied us with the entity
    private final EntityProvider entity_provider;
    // the modifiers which affect the entity
    private final Map<String, List<IExpiringModifier>> expiring = new HashMap<>();
    private final Map<String, AttributeCollection> attributes = new HashMap<>();
    private final Map<String, List<TagModifier>> tags = new HashMap<>();
    // effects on the entity
    private Map<Class<? extends IEntityEffect>, Map<String, IEntityEffect>> status_effects = new HashMap<>();
    private Map<AbstractAilment, AilmentTracker> ailment_tracker = new HashMap<>();
    // resources available to the entity
    private EntityResource health_resource;
    private EntityResource mana_resource;
    private EntityResource stamina_resource;
    // damage interaction causing entity death
    private DamageInteraction cause_of_death;

    // mark entity as no longer being used by the core
    private boolean invalid;

    // tracker for cooldowns on the entity
    private Map<String, Integer> cooldowns = new HashMap<>();

    // a process which has something at happen at the end of it
    private IActivity activity = null;

    public CoreEntity(LivingEntity entity, EntityProvider provider) {
        // apply basic bukkit initialization
        this.bukkit_uuid = entity.getUniqueId();
        this.bukkit_tasks = new LinkedList<>();
        // initialize the resources available to the entity
        this.health_resource = new EntityResource("HEALTH", this, false, 1);
        this.mana_resource = new EntityResource("MANA", this, false, 1);
        this.stamina_resource = new EntityResource("STAMINA", this, false, 1);
        // track who provided us with the entity
        this.entity_provider = provider;
        // translate core attributes to bukkit attributes
        this.bukkit_tasks.add(new CoreToBukkitAttributeTask(this)
                .runTaskTimer(RPGCore.inst(), 1, 20));
        // apply the burning effect on the entity
        this.bukkit_tasks.add(new BukkitImmolationTask(this)
                .runTaskTimer(RPGCore.inst(), 1, 19));
        // update the relevant effects on the entity
        this.bukkit_tasks.add(new EntityEffectTask(this)
                .runTaskTimer(RPGCore.inst(), 1, 10));
        // process the activity on the entity
        this.bukkit_tasks.add(new EntityActivityTask(this)
                .runTaskTimer(RPGCore.inst(), 1, 1));

    }

    /**
     * The current activity which the player has.
     *
     * @return the activity which was requested.
     */
    public IActivity getActivity() {
        return activity;
    }

    /**
     * Update the current activity, so long we have no
     * other activity working.
     *
     * @param activity the new activity to process.
     * @throws IllegalStateException if we already have an activity.
     */
    public void setActivity(IActivity activity) {
        if (activity == null) {
            // abandon current activity
            this.activity = null;
        } else if (this.activity != null) {
            // error since we already have an activity
            throw new IllegalArgumentException("There already is an activity!");
        } else {
            // inject our new activity
            this.activity = activity;
        }
    }

    /**
     * Interrupt the current activity, if there is one.
     *
     * @param interrupter who interrupted the activity.
     * @return the interrupted activity, if there is one.
     */
    public IActivity interruptActivity(CoreEntity interrupter) {
        // ensure we got an activity to interrupt
        if (this.activity == null)
            return null;
        // interrupt the activity and offer it up
        IActivity activity = this.activity;
        activity.interrupt(interrupter);
        return activity;
    }

    /**
     * Put the given ID on a cooldown for a limited
     * duration.
     *
     * @param id       which ID to put on cooldown.
     * @param duration ticks to be on cooldown.
     */
    public void setCooldown(String id, int duration) {
        this.cooldowns.put(id, RPGCore.inst().getTimestamp() + duration);
    }

    /**
     * Fetch the remaining cooldown duration.
     *
     * @param id which ID to check
     * @return ticks left on cooldown
     */
    public int getCooldown(String id) {
        int cooldown = this.cooldowns.getOrDefault(id, 0);
        return Math.max(0, cooldown - RPGCore.inst().getTimestamp());
    }

    /**
     * Generate a context for the given skill.
     *
     * @param skill the skill we want a context for.
     * @return the context that was just created.
     */
    public SkillContext createSkillContext(CoreSkill skill) {
        return new SkillContext(this);
    }

    /**
     * Retrieve the trackers belonging to relevant ailments.
     *
     * @return a mapping of trackers to ailments.
     */
    public Map<AbstractAilment, AilmentTracker> getAilmentTracker() {
        return ailment_tracker;
    }

    /**
     * A collection of expiring modifiers which share a certain
     * identifier group.
     *
     * @param id the ID of the expiring modifier
     * @return all expiring modifiers sharing the id
     */
    public List<IExpiringModifier> getExpiringModifiers(String id) {
        return expiring.computeIfAbsent(id, (k -> new ArrayList<>()));
    }

    /**
     * Resource serving as health, cannot recover after reaching zero, health
     * is consumed after surface level resources were processed first.
     * <p>
     * Can be reserved by a skill with a permanent effect.
     *
     * @return health resource.
     */
    public EntityResource getHealth() {
        return health_resource;
    }

    /**
     * Used by skills to pay a certain cost.
     * <p>
     * Can be reserved by a skill with a permanent effect.
     *
     * @return mana resource.
     */
    public EntityResource getMana() {
        return mana_resource;
    }

    /**
     * Used for sprinting and weapon attacks.
     *
     * @return stamina resource.
     */
    public EntityResource getStamina() {
        return stamina_resource;
    }

    /**
     * Ward is gained as a status effect, when the effect is acquired
     * the preceding ward effect is forcefully removed.
     *
     * @return the current ward effect we have.
     */
    public EntityWard getWard() {
        // check if we got any ward effect
        Map<String, IEntityEffect> effects = this.status_effects.get(EntityWard.class);
        if (effects == null || effects.isEmpty())
            return null;
        // offer up the only ward effect we should have
        return (EntityWard) effects.values().iterator().next();
    }

    /**
     * Fetch the effect which matches the ID.
     *
     * @param id the identifier of the effect.
     * @return the effect with that id, if any.
     */
    public IEntityEffect getEffect(String id) {
        // search for any effect with the given ID
        for (Map<String, IEntityEffect> effects : this.status_effects.values()) {
            IEntityEffect effect = effects.get(id);
            if (effect != null)
                return effect;
        }
        // if nothing matched offer up null instead
        return null;
    }

    /**
     * A listing of all effects on the entity.
     *
     * @return the effects on the entity.
     */
    public Map<Class<? extends IEntityEffect>, Map<String, IEntityEffect>> getStatusEffects() {
        return status_effects;
    }

    /**
     * Acquire an effect of a certain ID, this call will destroy
     * any effect which matches the ID of this effect.
     *
     * @param id     the identifier of the effect
     * @param effect the effect to be acquired
     */
    public void addEffect(String id, IEntityEffect effect) {
        // destroy any effect which matches this ID
        for (Map<String, IEntityEffect> effects : this.status_effects.values())
            effects.remove(id);

        // track the effect which is requested to be added
        this.status_effects.computeIfAbsent(effect.getClass(), (k -> new HashMap<>())).put(id, effect);
    }

    /**
     * Fetch who has provided this entity.
     *
     * @return who provided this entity.
     */
    public EntityProvider getEntityProvider() {
        return entity_provider;
    }

    /**
     * Retrieve the attribute collection tracked on this entity.
     *
     * @param attribute the attribute to read
     * @return the collection backing it up.
     */
    public AttributeCollection getAttribute(String attribute) {
        return this.attributes.computeIfAbsent(attribute, (k -> {
            // create the collection to hold modifiers for this attribute
            AttributeCollection collection = new AttributeCollection(this);
            // configure the collection for this attribute specifically
            RPGCore.inst().getAttributeManager()
                    .getIndex()
                    .get(attribute)
                    .setup(collection);
            // offer up the collection we created
            return collection;
        }));
    }

    /**
     * Evaluate the value of an attribute, relative to what sort of
     * type it has.
     *
     * @param attribute which attribute to look up.
     * @return the value of the given attribute
     */
    @Override
    public double evaluateAttribute(String attribute) {
        return getAttribute(attribute).evaluate();
    }

    /**
     * Check if a certain tag is present or not.
     *
     * @param tag the tag to check for.
     * @return whether we have the tag or not.
     */
    @Override
    public boolean checkForTag(String tag) {
        List<TagModifier> modifiers = tags.get(tag);
        if (modifiers == null) return false;
        modifiers.removeIf(TagModifier::isExpired);
        return !modifiers.isEmpty();
    }

    /**
     * Acquire a certain tag, until it is expired.
     *
     * @param tag the tag we've acquired.
     * @return the modifier backing up the tag.
     */
    public TagModifier grantTag(String tag) {
        TagModifier gained = new TagModifier();
        this.tags.computeIfAbsent(tag, (k -> new ArrayList<>())).add(gained);
        return gained;
    }

    /**
     * Verify if the core entity associated with the bukkit uuid is this
     * instance.
     *
     * @return true if we are the valid active instance.
     */
    public boolean isRegistered() {
        return RPGCore.inst().getEntityManager().getEntity(this.bukkit_uuid) == this;
    }

    /**
     * Clean-up service when the entity is no longer to be associated
     * with the core.
     *
     * @see EntityManager#unregister(UUID) This method is for internal usage only!
     */
    public void remove() {
        // drop all tasks from the scheduler
        for (BukkitTask bukkit_task : this.bukkit_tasks)
            bukkit_task.cancel();
        this.bukkit_tasks.clear();
        // abandon if tracked thorough instance
        this.invalid = true;
    }

    /**
     * Unique identifier of the bukkit entity which is associated
     * with this core entity.
     *
     * @return the bukkit entity uuid we are associated with.
     */
    public UUID getUniqueId() {
        return bukkit_uuid;
    }

    /**
     * Retrieve the bukkit entity we are coupled to, do note that
     * under normal circumstances this method will always be able
     * to return the
     *
     * @return the entity backing us up.
     */
    public LivingEntity getEntity() {
        return (LivingEntity) Bukkit.getEntity(getUniqueId());
    }

    /**
     * Invoke the death logic of this entity.
     *
     * @param interaction what killed the entity.
     */
    public void die(DamageInteraction interaction) {
        // track this as the last damage cause
        this.cause_of_death = interaction;
        // force kill the entity backing us up
        getEntity().setHealth(0d);
        // mark entity for being unloaded
        RPGCore.inst().getEntityManager().unregister(this.getUniqueId());
    }

    /**
     * The last interaction which dealt damage to this entity, causing
     * them to die.
     *
     * @return the interaction which caused their death.
     */
    public DamageInteraction getCauseOfDeath() {
        return cause_of_death;
    }

    /**
     * We are invalid, if the core abandoned this instance.
     *
     * @return true if instance is abandoned.
     */
    public boolean isInvalid() {
        return invalid;
    }

    /**
     * Whether this entity should be targeted by core specific
     * mechanics such as ray-casting, selectors, player ux etc
     *
     * @return true if we may be targeted
     */
    public boolean isAllowTarget() {
        return !isInvalid();
    }

    /**
     * Retrieve the location which we are located at.
     *
     * @return where we are located.
     */
    @Override
    public Location getLocation() {
        return this.getEntity().getLocation();
    }
}
