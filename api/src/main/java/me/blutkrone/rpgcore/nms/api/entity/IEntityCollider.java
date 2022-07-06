package me.blutkrone.rpgcore.nms.api.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * An entity intended to serve as a collider for another.
 */
public interface IEntityCollider {

    /**
     * Link with an entity, the relevant events are delegated to
     * the linked entity then.
     *
     * @param entity who has moved.
     */
    void link(Entity entity);

    /**
     * Teleport to the given coordinate.
     *
     * @param location target location.
     */
    void move(Location location);

    /**
     * Highlight the linked entity for the given duration.
     *
     * @param time how long to highlight.
     */
    void highlight(int time);

    /**
     * What size to rescale to, this is an unknown metric
     * between 1 to 127.
     *
     * @param size hitbox size
     */
    void resize(int size);

    /**
     * Mark as no longer active, will destroy the entity. The
     * entity may also be destroyed when the linked entity is
     * no longer available.
     */
    void destroy();

    /**
     * Check if we are still physically present.
     *
     * @return true if physically present.
     */
    boolean isActive();
}
