package me.blutkrone.rpgcore.nms.api.mob;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * A common base for a living entity.
 */
public interface IEntityBase {

    /**
     * Attempt to make a barrier soak the damage, a barrier always
     * soaks any damage. The return value implies whether there is
     * a barrier available.
     *
     * @param damage the damage we want to soak.
     * @return whether the creature has any barrier.
     */
    boolean doBarrierDamageSoak(int damage);

    /**
     * Generate rage on the entity
     *
     * @param source  who generated the rage
     * @param amount  how much rage is generated
     * @param maximum do not raise rage limit beyond this
     * @param focus   focus lowers rage generated by other targets
     * @param forced  ignores quick-swap cooldown
     */
    void enrage(LivingEntity source, double amount, double maximum, double focus, boolean forced);

    /**
     * Retain the current rage, but focus it on another player.
     *
     * @param target Who to shift to
     * @param focus  What focus to apply on the rage
     */
    void rageTransfer(LivingEntity target, double focus);

    /**
     * Who is the current entity we hold rage against.
     *
     * @return current rage holder.
     */
    LivingEntity getRageEntity();

    /**
     * The rage value we are holding currently.
     *
     * @return current rage value.
     */
    double getRageValue();

    /**
     * Display the entity as aggressive.
     *
     * @param aggressive whether to show as aggressive.
     */
    void setAggressive(boolean aggressive);

    /**
     * Add a routine to the entity, do note that
     *
     * @param namespace
     * @param routine
     */
    void addRoutine(String namespace, AbstractEntityRoutine routine);

    /**
     * Even with multiple death routines queried, only one will be
     * invoked.
     * <p>
     * If we've got a death routine, the entity will not die before
     * we've processed the routine. If no routine can be invoked, the
     * entity will die immediately.
     * <p>
     * Upon completion of the death routine, we will attempt to blame
     * the last hitter or the nearest player.
     *
     * @param routine a death routine.
     */
    void addDeathRoutine(AbstractEntityRoutine routine);

    /**
     * Attempt to talk towards the given location, do note
     * that the pathfinder will fail if a walk request has
     * been made before.
     *
     * @param entity walks to the location (does not update.)
     * @param speed  how quick to travel
     * @return true if the prior walking had a lower-equal priority.
     */
    boolean walkTo(LivingEntity entity, double speed);

    /**
     * Attempt to talk towards the given location, do note
     * that the pathfinder will fail if a walk request has
     * been made before.
     *
     * @param where walks to the location
     * @param speed how quick to travel
     * @return true if the prior walking had a lower-equal priority.
     */
    boolean walkTo(Location where, double speed);

    /**
     * Strolls on land within the given parameters.
     *
     * @param minimum lower stroll radius
     * @param maximum upper stroll radius
     * @param speed   movement speed
     * @return whether we could stroll somewhere.
     */
    boolean stroll(int minimum, int maximum, double speed, Predicate<Location> valid);

    /**
     * If the entity is walking, makes them stop that.
     */
    void stopWalk();

    /**
     * Make the entity look at a location.
     *
     * @param entity who to look at.
     */
    void look(Entity entity);

    /**
     * Make the entity look at a location.
     *
     * @param location where to look at.
     */
    void look(Location location);

    /**
     * Check whether we can sense the given entity.
     *
     * @param other who are we checking.
     * @return whether we can sense the entity.
     */
    boolean canSense(LivingEntity other);

    /**
     * Check whether we can see the other entity.
     *
     * @param other who are we checking.
     * @return whether we have a direct line-of-sight.
     */
    boolean canSee(LivingEntity other);

    /**
     * Retrieve the bukkit handle which backs up the entity.
     *
     * @return the bukkit handle we are backed up by.
     */
    LivingEntity getBukkitHandle();

    /**
     * Check whether we are walking or not.
     *
     * @return true if we are walking.
     */
    boolean isWalking();

    /**
     * Death phase cannot be aborted, serves as one final
     * mechanic to finish with.
     *
     * @return whether we are running the death phase
     */
    boolean isInDeathSequence();

    /**
     * A request for the mob to enter their "death phase", should
     * we have no death phase we can instantaneously perish.
     * <p>
     * This method does nothing if called multiple times.
     *
     * @param callback response once death sequence is completed.
     * @return true if entity is allowed to die
     */
    boolean doDeathSequence(Runnable callback);

    /**
     * Reset rage of the creature.
     */
    void resetRage();
}
