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
import me.blutkrone.rpgcore.attribute.TagModifierTimed;
import me.blutkrone.rpgcore.damage.ailment.AbstractAilment;
import me.blutkrone.rpgcore.damage.ailment.AilmentTracker;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.EntityManager;
import me.blutkrone.rpgcore.entity.resource.EntityResource;
import me.blutkrone.rpgcore.entity.resource.EntityWard;
import me.blutkrone.rpgcore.entity.tasks.*;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.activity.ISkillActivity;
import me.blutkrone.rpgcore.skill.behaviour.BehaviourEffect;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.mechanic.BarrierMechanic;
import me.blutkrone.rpgcore.skill.mechanic.InstantMechanic;
import me.blutkrone.rpgcore.skill.proxy.AbstractSkillProxy;
import me.blutkrone.rpgcore.skill.trigger.AbstractCoreTrigger;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

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
    protected final UUID bukkit_uuid;
    // a "provider" which supplied us with the entity
    protected final EntityProvider entity_provider;
    // the modifiers which affect the entity
    protected final Map<String, List<IExpiringModifier>> expiring = new HashMap<>();
    protected final Map<String, AttributeCollection> attributes = new HashMap<>();
    protected final Map<String, List<TagModifier>> tags = new HashMap<>();
    // effects on the entity
    protected Map<String, IEntityEffect> status_effects = new HashMap<>();
    protected Map<AbstractAilment, AilmentTracker> ailment_tracker = new HashMap<>();
    // resources available to the entity
    protected EntityResource health_resource;
    protected EntityResource mana_resource;
    protected EntityResource stamina_resource;
    // damage interaction causing entity death
    protected DamageInteraction cause_of_death;
    // mark entity as no longer being used by the core
    protected boolean invalid;
    // tracker for cooldowns on the entity
    protected Map<String, Integer> cooldowns = new HashMap<>();
    // a process which has something at happen at the end of it
    protected IActivity activity = null;
    // proxies provided by skills
    protected List<AbstractSkillProxy> proxies = new ArrayList<>();
    // parent-child logic
    protected List<UUID> children = new ArrayList<>();
    protected UUID parent;
    // tags we are involved with
    protected List<String> tags_self = new ArrayList<>();
    protected List<String> tags_hostile = new ArrayList<>();
    protected List<String> tags_friendly = new ArrayList<>();
    // a pseudo hint to pull from an activity
    protected String focus_hint = "none";
    protected int focus_hint_until = -1;
    // track the pipelines for actions we've requested
    protected List<CoreAction.ActionPipeline> actions = new ArrayList<>();
    // level of this creature
    protected int current_level;

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
        // processes the proxies we are using
        this.bukkit_tasks.add(new EntityProxyTask(this)
                .runTaskTimer(RPGCore.inst(), 1, 1));
        // apply recovery and update maximum capacities
        this.bukkit_tasks.add(new EntityRecoveryTask(this)
                .runTaskTimer(RPGCore.inst(), 1, 10));
        // tick skill behaviours we've queried
        this.bukkit_tasks.add(new EntitySkillTask(this)
                .runTaskTimer(RPGCore.inst(), 1, 1));
    }

    /**
     * Current level of the entity.
     *
     * @return current level
     */
    public int getCurrentLevel() {
        return Math.max(current_level, 1);
    }

    /**
     * Current level of the entity.
     *
     * @param current_level new level
     */
    public void setCurrentLevel(int current_level) {
        this.current_level = Math.max(1, current_level);
    }

    /**
     * Check for an instant cast for the given skill, and if requested
     * consume it.
     *
     * @param skill   the skill we're checking
     * @param consume whether to consume the instant cast
     * @return true if we have an instant cast
     */
    public boolean hasInstantCast(CoreSkill skill, boolean consume) {
        for (IEntityEffect effect : getStatusEffects().values()) {
            // ensure we are an instant cast
            if (!(effect instanceof InstantMechanic.Effect)) {
                continue;
            }
            // ensure that the effect can instant cast this
            InstantMechanic.Effect casted = (InstantMechanic.Effect) effect;
            if (!casted.doesMatch(skill)) {
                continue;
            }
            // consume the instant cast if applicable
            if (consume) {
                casted.setConsumed();
            }
            // we have an instant cast to use
            return true;
        }
        // no instant cast matches this
        return false;
    }

    /**
     * Proliferate an event to every passive behaviour that
     * has a trigger of the given type.
     *
     * @param clazz the trigger type to check
     * @param event the event acquired
     */
    public void proliferateTrigger(Class<? extends AbstractCoreTrigger> clazz, Object event) {
        for (IEntityEffect effect : getStatusEffects().values()) {
            if (effect instanceof BehaviourEffect) {
                BehaviourEffect casted = (BehaviourEffect) effect;
                AbstractCoreTrigger trigger = casted.getBehaviour().getTrigger();

                if (trigger.getClass() == clazz) {
                    // ensure not on cooldown
                    String cdId = trigger.cooldown_id.evaluate(casted.getContext());
                    if (casted.getContext().getCoreEntity().getCooldown(cdId) > 0) {
                        continue;
                    }
                    // grab info or create new info
                    AbstractCoreTrigger.TriggerInfo info = casted.getInfo();
                    if (info == null) {
                        casted.setInfo(info = trigger.createInfo());
                    }
                    // update trigger and check if we can invoke
                    if (trigger.update(casted.getContext(), event, info)) {
                        casted.setInfo(null);
                        casted.getBehaviour().doBehaviour(casted.getContext());
                        // update cooldown to be applied
                        int cooldown = trigger.cooldown_time.evalAsInt(casted.getContext());
                        double reduction = trigger.cooldown_reduction.evalAsDouble(casted.getContext());
                        double recovery = trigger.cooldown_recovery.evalAsDouble(casted.getContext());
                        int result = (int) (cooldown * Math.max(0d, 1d - reduction) / Math.max(0.01d, 1d + recovery));
                        if (result > 0) {
                            casted.getContext().getCoreEntity().setCooldown(cdId, result);
                        }
                    }
                }
            }
        }
    }

    /**
     * Soak X amount of damage with barriers that are stalling
     * execution of other abilities.
     *
     * @param damage the damage we've taken
     * @return whether we soaked damage
     */
    public boolean soakForBarrier(int damage) {
        boolean barrier = false;

        // check for barrier from mob AI
        if (this instanceof CoreMob) {
            IEntityBase base = ((CoreMob) this).getBase();
            barrier = base.doBarrierDamageSoak(damage);
        }

        // check for barrier from skill activity
        IActivity activity = this.getActivity();
        if (activity instanceof ISkillActivity) {
            barrier |= ((ISkillActivity) activity).doBarrierDamageSoak(damage);
        }

        // check for barrier from passive behaviours
        for (CoreAction.ActionPipeline pipeline : this.getActions()) {
            BarrierMechanic.ActiveBarrier active = pipeline.getBarrier();
            if (active != null) {
                active.damage -= damage;
            }
        }

        // whether we soaked any damage
        return barrier;
    }

    /**
     * Actions which we are currently operating.
     *
     * @return the pipelines which we do have.
     */
    public List<CoreAction.ActionPipeline> getActions() {
        return actions;
    }

    /**
     * Does nothing by default, but may be used by derivations.
     */
    public void updateSkills() {

    }

    /**
     * A hint to show on the UX for focus information, this will override
     * the activity information.
     *
     * @return hint to be rendered, may be null.
     */
    public String getFocusHint() {
        // get rid of the hint if the timestamp expired
        if (this.focus_hint_until < RPGCore.inst().getTimestamp()) {
            this.focus_hint = null;
        }
        // offer up the hint or nothing
        return this.focus_hint;
    }

    /**
     * A hint to show on the UX for focus information, this will override
     * the activity information.
     *
     * @param hint     the hint we are given
     * @param duration how many ticks the hint lasts
     */
    public void giveFocusHint(String hint, int duration) {
        this.focus_hint = hint;
        this.focus_hint_until = RPGCore.inst().getTimestamp() + duration;
    }

    /**
     * A view of all child entities.
     *
     * @return child entities.
     */
    public Set<CoreEntity> getChildren() {
        Set<CoreEntity> children = new HashSet<>();
        this.children.removeIf(uuid -> {
            CoreEntity entity = RPGCore.inst().getEntityManager().getEntity(uuid);
            if (entity == null) {
                return true;
            }
            children.add(entity);
            return false;
        });
        return children;
    }

    /**
     * Tags that are identifying this entity.
     *
     * @return tags of the entity.
     */
    public List<String> getMyTags() {
        return tags_self;
    }

    /**
     * We are friendly, if the target has these tags. This
     * works bi-directionally.
     *
     * @return tags to be friendly towards.
     */
    public List<String> getTagsFriendly() {
        return tags_friendly;
    }

    /**
     * We are hostile, if the target has these tags. This
     * works bi-directionally.
     *
     * @return tags to be hostile towards.
     */
    public List<String> getTagsHostile() {
        return tags_hostile;
    }

    /**
     * The unique identifier of the parent, do note that the
     * parent may be dead already which can cause there to be
     * no associated entity.
     *
     * @return identifier of parent.
     */
    public UUID getParent() {
        return parent;
    }

    /**
     * Establishes a parent-child relationship, this is
     * handled bi-directionally.
     *
     * @param parent entity that will become our parent.
     */
    public void setParent(CoreEntity parent) {
        this.parent = parent.getUniqueId();
        parent.children.add(this.getUniqueId());
    }

    /**
     * Establishes a parent-child relationship, this is
     * handled bi-directionally.
     *
     * @param child entity that will become our child.
     */
    public void addChild(CoreEntity child) {
        child.parent = this.getUniqueId();
        this.children.add(child.getUniqueId());
    }

    /**
     * Proxies which were created by skills to invoke
     * certain logic in some way.
     *
     * @return all proxies
     */
    public List<AbstractSkillProxy> getProxies() {
        return proxies;
    }

    @Override
    public void addPipeline(CoreAction.ActionPipeline pipeline) {
        this.getActions().add(pipeline);
    }

    @Override
    public List<CoreAction.ActionPipeline> getPipelines() {
        return this.getActions();
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
     */
    public void interruptActivity() {
        if (this.activity != null) {
            this.activity.interrupt();
            this.activity = null;
        }
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
     * <br>
     * There are the following causes to create a skill context:
     * <ul>
     * <li>Used the skillbar to cast a skill</li>
     * <li>Passive behaviour was triggered</li>
     * <li>Player UX wants to check usability</li>
     * </ul>
     *
     * @param skill the skill we want a context for.
     * @return the context that was just created.
     */
    public SkillContext createSkillContext(CoreSkill skill) {
        return new SkillContext(this, skill);
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
     * <br>
     * Can be reserved by a skill with a permanent effect.
     *
     * @return health resource.
     */
    public EntityResource getHealth() {
        return health_resource;
    }

    /**
     * Used by skills to pay a certain cost.
     * <br>
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
        Map<String, IEntityEffect> effects = getStatusEffects();
        if (effects == null || effects.isEmpty())
            return null;

        // search for wards
        for (IEntityEffect effect : effects.values()) {
            if (effect instanceof EntityWard) {
                return (EntityWard) effect;
            }
        }

        // offer up the only ward effect we should have
        return null;
    }

    /**
     * Fetch the effect which matches the ID.
     *
     * @param id the identifier of the effect.
     * @return the effect with that id, if any.
     */
    public IEntityEffect getEffect(String id) {
        return getStatusEffects().get(id);
    }

    /**
     * A listing of all effects on the entity.
     *
     * @return the effects on the entity.
     */
    public Map<String, IEntityEffect> getStatusEffects() {
        // abandon all effects no longer considered to be valid
        status_effects.entrySet().removeIf(effect -> !effect.getValue().isValid());
        // effects that are still fine
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
        Map<String, IEntityEffect> effects = getStatusEffects();
        // override any effect with the same ID
        effects.put(id, effect);
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
        return this.attributes.computeIfAbsent(attribute.toLowerCase(), (k -> {
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
        List<TagModifier> modifiers = tags.get(tag.toLowerCase());
        if (modifiers == null) return false;
        modifiers.removeIf(TagModifier::isExpired);
        return !modifiers.isEmpty();
    }

    @Override
    public CoreEntity getCoreEntity() {
        return this;
    }

    @Override
    public IOrigin getOrigin() {
        return new SnapshotOrigin(getLocation());
    }

    @Override
    public void addProxy(AbstractSkillProxy proxy) {
        this.getProxies().add(proxy);
    }

    /**
     * Acquire a certain tag, until it is expired.
     *
     * @param tag the tag we've acquired.
     * @return a modifier which we can expire manually.
     */
    public TagModifier grantTag(String tag) {
        TagModifier gained = new TagModifier();
        this.tags.computeIfAbsent(tag.toLowerCase(), (k -> new ArrayList<>())).add(gained);
        return gained;
    }

    /**
     * Acquire a certain tag, which expires naturally after a certain
     * duration has passed.
     *
     * @param tag      the tag we've acquired.
     * @param duration how long the tag lasts.
     * @return a modifier which we can expire manually.
     */
    public TagModifier grantTag(String tag, int duration) {
        TagModifier gained = new TagModifierTimed(duration);
        this.tags.computeIfAbsent(tag.toLowerCase(), (k -> new ArrayList<>())).add(gained);
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
     * with the core. Do <b>NOT</b> call this directly, EVER!
     *
     * @see EntityManager#unregister(UUID) This method is for internal usage only!
     */
    public void remove() {
        // request proxies to be destructed
        this.proxies.removeIf(proxy -> {
            proxy.pleaseCancelThis();
            return proxy.update();
        });
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
        // if we hold the target rage, reset it
        interaction.shiftRageBlame();
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
     * Check if we are considered friendly to the other entity, this
     * is expected to be bi-directional.
     *
     * @param context who to compare against
     * @return whether we are friendly
     */
    public boolean isFriendly(IContext context) {
        CoreEntity other = context.getCoreEntity();
        return other != null && isFriendly(other);
    }

    /**
     * Check if we are considered hostile to the other entity, this
     * is expected to be bi-directional.
     *
     * @param context who to compare against
     * @return whether we are hostile
     */
    public boolean isHostile(IContext context) {
        CoreEntity other = context.getCoreEntity();
        return other != null && isHostile(other);
    }

    /**
     * Check if we are considered friendly to the other entity, this
     * is expected to be bi-directional.
     *
     * @param other who to compare against
     * @return whether we are friendly
     */
    public boolean isFriendly(CoreEntity other) {
        // relationship fail if null
        if (other == null) {
            return false;
        }

        // grab parent of this entity
        CoreEntity a = this;
        while (a.getParent() != null) {
            CoreEntity parent = RPGCore.inst().getEntityManager().getEntity(a.getParent());
            if (parent == null) {
                break;
            }
            a = parent;
        }
        // grab parent of other entity
        CoreEntity b = other;
        while (b.getParent() != null) {
            CoreEntity parent = RPGCore.inst().getEntityManager().getEntity(b.getParent());
            if (parent == null) {
                break;
            }
            b = parent;
        }
        // we are friendly to ourself
        if (a == b) {
            return true;
        }
        // check if either is friendly to the other
        for (String tag : a.tags_friendly) {
            if (b.tags_self.contains(tag)) {
                return true;
            }
        }
        for (String tag : b.tags_friendly) {
            if (a.tags_self.contains(tag)) {
                return true;
            }
        }
        // otherwise we are not considered friendly
        return false;
    }

    /**
     * Check if we are considered hostile to the other entity, this
     * is expected to be bi-directional.
     *
     * @param other who to compare against
     * @return whether we are hostile
     */
    public boolean isHostile(CoreEntity other) {
        // relationship fail null
        if (other == null) {
            return false;
        }

        // grab parent of this entity
        CoreEntity a = this;
        while (a.getParent() != null) {
            CoreEntity parent = RPGCore.inst().getEntityManager().getEntity(a.getParent());
            if (parent == null) {
                break;
            }
            a = parent;
        }
        // grab parent of other entity
        CoreEntity b = other;
        while (b.getParent() != null) {
            CoreEntity parent = RPGCore.inst().getEntityManager().getEntity(b.getParent());
            if (parent == null) {
                break;
            }
            b = parent;
        }
        // cannot be hostile to ourself or allies
        if (a == b || isFriendly(other)) {
            return false;
        }
        // check if either is hostile to the other
        for (String tag : a.tags_hostile) {
            if (b.tags_self.contains(tag)) {
                return true;
            }
        }
        for (String tag : b.tags_hostile) {
            if (a.tags_self.contains(tag)) {
                return true;
            }
        }
        // otherwise we are not considered hostile
        return false;
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

    /**
     * Retrieve location of the head, as defined by who provided
     * us with the backing mob.
     *
     * @return Who provided the head.
     */
    public Location getHeadLocation() {
        return this.getEntityProvider().getHeadLocation(this.getEntity());
    }

    @Override
    public List<CoreEntity> rayCastEntities(double distance, double size) {
        List<CoreEntity> output = new ArrayList<>();
        Location location = getHeadLocation();
        org.bukkit.util.Vector direction = location.getDirection();
        // grabs all entities within casting line
        List<Entity> entities = new ArrayList<>();
        getWorld().rayTraceEntities(location, direction, distance, size, entities::add);
        for (Entity entity : entities) {
            CoreEntity core_entity = RPGCore.inst().getEntityManager().getEntity(entity.getUniqueId());
            if (core_entity != null) {
                output.add(core_entity);
            }
        }
        // offer up our targets
        return output;
    }

    @Override
    public Optional<Block> rayCastBlock(double distance) {
        Location location = getHeadLocation();
        org.bukkit.util.Vector direction = location.getDirection();
        // throw a cast to find a block
        RayTraceResult result = getWorld().rayTraceBlocks(location, direction, distance, FluidCollisionMode.NEVER, true);
        // offer block or nothing
        Block block = null;
        if (result != null) {
            block = result.getHitBlock();
        }
        // safely wrap the object
        return Optional.ofNullable(block);
    }

    @Override
    public boolean hasLineOfSight(IOrigin other) {
        Location start = getHeadLocation().clone();
        Location finish = other instanceof CoreEntity ? ((CoreEntity) other).getHeadLocation().clone() : other.getLocation().clone();
        Vector direction = finish.clone().subtract(start.clone()).toVector().normalize();

        // throw a cast to find a block
        RayTraceResult result = getWorld().rayTraceBlocks(start, direction, this.distance(other) + 1, FluidCollisionMode.NEVER, true);
        // check if we've got a block
        Block block = null;
        if (result != null) {
            block = result.getHitBlock();
        }
        // if no block exists or block is air, we got a line-of-sight
        return block == null || block.getType().isAir();
    }

    @Override
    public String toString() {
        return String.format("CoreEntity{uuid=%s;name=%s;class=%s}",
                getUniqueId(), getEntity().getName(), getClass().getSimpleName());
    }
}
