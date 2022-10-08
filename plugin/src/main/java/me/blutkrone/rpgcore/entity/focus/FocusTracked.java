package me.blutkrone.rpgcore.entity.focus;


import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * A "tracked" focus element for a player.
 */
public class FocusTracked {
    // the priority for this focus
    public int priority;
    // distance before losing focus
    public double distance;
    // duration before focus removed
    public int duration;

    // who is focus upon
    private CoreEntity target_entity;
    // tick to lose focus at
    private int expire_time;

    /**
     * A wrapper manager for handling "target" entities.
     *
     * @param priority the priority relative to other targets
     * @param distance the distance before losing track
     * @param duration maximum allowed duration to hold target
     */
    public FocusTracked(int priority, double distance, int duration) {
        this.priority = priority;
        this.distance = distance;
        this.duration = duration;
    }

    /**
     * Validate the current entity target, removing it
     * should they no longer be necessary.
     */
    public void validate(Location self_location) {
        // do not validate null entry
        if (this.target_entity == null)
            return;
        Entity handle = Bukkit.getEntity(this.target_entity.getUniqueId());
        if (handle == null) {
            // ensure that the living handle exists
            this.target_entity = null;
        } else if (this.expire_time < RPGCore.inst().getTimestamp()) {
            // the entity exceed the maximum time to be picked
            this.target_entity = null;
        } else if (handle.getLocation().distanceSquared(self_location) > distance * distance) {
            // the entity moved too far away from the target
            this.target_entity = null;
        }
    }

    /**
     * Entity who is the current target.
     *
     * @return target or null
     */
    public CoreEntity getTargetEntity() {
        // drop target if duration has run out
        if (this.expire_time < RPGCore.inst().getTimestamp()) {
            this.target_entity = null;
        }
        // drop target if no longer registered
        if (this.target_entity != null && this.target_entity.isInvalid()) {
            this.target_entity = null;
        }
        // offer up the target we've found
        return this.target_entity;
    }

    /**
     * Update the target entity, reset timer.
     *
     * @param target_entity updated target
     */
    public void setTargetEntity(CoreEntity target_entity) {
        this.target_entity = target_entity;
        this.expire_time = RPGCore.inst().getTimestamp() + this.duration;
    }
}