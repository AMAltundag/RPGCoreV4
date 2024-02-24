package me.blutkrone.rpgcore.api.entity;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

public interface EntityProvider {

    /**
     * Create an entity at the given location.
     *
     * @param where where to create the entity
     * @param args  instructions on how to create the entity
     * @return the entity which was created
     */
    LivingEntity create(Location where, Object... args);

    /**
     * The bounds of the backing creature its shape.
     *
     * @return the bounds we are dealing with.
     */
    BoundingBox getBounds(LivingEntity entity);

    /**
     * Retrieve the location of the head of the mob, this refers to the
     * primary ray-throwing head.
     *
     * @param entity Location of the head.
     * @return Location of head of the mob.
     */
    Location getHeadLocation(LivingEntity entity);
}