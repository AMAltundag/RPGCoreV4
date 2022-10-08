package me.blutkrone.rpgcore.api;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * An origin has a location associated with it, primarily
 * used by the skill system to have a uniform interface to
 * contain locations.
 */
public interface IOrigin {

    /**
     * Disconnect the origin and track it persistently by
     * location, the {@link #getLocation()} method will be
     * mutable and persistent.
     *
     * @return the isolated origin.
     */
    default IOrigin isolate() {
        return new SnapshotOrigin(getLocation().clone());
    }

    /**
     * The implementing class transformed into a location.
     *
     * @return the location of this origin.
     */
    Location getLocation();

    /**
     * Offer up the distance between the two given locations.
     *
     * @param origin the other origin to compare against.
     * @return distance between origins, max be {@link Double#MAX_VALUE}
     *         should the origins be incompatible.
     */
    default double distance(IOrigin origin) {
        try {
            Location a = getLocation();
            Location b = origin.getLocation();
            // if either origin cannot be mapped to a location
            if (a == null || b == null) {
                return Double.MAX_VALUE;
            }
            // if the origins are in different worlds
            if (a.getWorld() != b.getWorld()) {
                return Double.MAX_VALUE;
            }
            // just offer the distance
            return a.distance(b);
        } catch (Exception e) {
            // fallback if one origin is bad.
            return Double.MAX_VALUE;
        }
    }

    /**
     * Retrieve all entities near this origin.
     *
     * @param radius the radius to search
     * @return all entities found
     */
    default List<CoreEntity> getNearby(double radius) {
        List<CoreEntity> output = new ArrayList<>();
        // ensure the location can be searched
        Location where = getLocation();
        if (where == null || where.getWorld() == null) {
            return output;
        }
        // fetch all entities within radius
        Collection<Entity> entities = where.getWorld().getNearbyEntities(where, radius, radius, radius);
        for (Entity entity : entities) {
            CoreEntity core_entity = RPGCore.inst().getEntityManager().getEntity(entity.getUniqueId());
            if (core_entity != null) {
                output.add(core_entity);
            }
        }
        // offer up our targets
        return output;
    }

    /**
     * World that backs up the location.
     *
     * @return relevant world.
     */
    default World getWorld() {
        World world = getLocation().getWorld();
        if (world == null) {
            throw new NullPointerException("No world in location!");
        }
        return world;
    }

    /**
     * Run a ray-cast that fetches all entities.
     *
     * @param distance maximum distance of ray-cast
     * @param size extra size of ray-cast
     * @return all entities within cast
     */
    default List<CoreEntity> rayCastEntities(double distance, double size) {
        List<CoreEntity> output = new ArrayList<>();
        Location location = getLocation();
        Vector direction = location.getDirection();
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

    /**
     * Run a ray-cast that fetches the target block.
     *
     * @param distance maximum distance of ray-cast
     * @return block that was casted
     */
    default Optional<Block> rayCastBlock(double distance) {
        Location location = getLocation();
        Vector direction = location.getDirection();
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

    /**
     * Check if there exists a line-of-sight between these
     * two locations.
     *
     * @param other the other candidate to check against
     * @return whether we have a line-of-sight
     */
    default boolean hasLineOfSight(IOrigin other) {
        Location location = getLocation().clone();
        Vector direction = other.getLocation().clone().subtract(getLocation()).toVector().normalize();
        // throw a cast to find a block
        RayTraceResult result = getWorld().rayTraceBlocks(location, direction, this.distance(other), FluidCollisionMode.NEVER, true);
        // check if we've got a block
        Block block = null;
        if (result != null) {
            block = result.getHitBlock();
        }
        // if no block exists or block is air, we got a line-of-sight
        return block == null || block.getType().isAir();
    }

    /**
     * An origin which is a snapshot of a location.
     */
    class SnapshotOrigin implements IOrigin {

        private final Location location;

        public SnapshotOrigin(Location location) {
            this.location = location;
        }

        @Override
        public Location getLocation() {
            return location;
        }
    }
}
