package me.blutkrone.rpgcore.bbmodel.active.component;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;

import java.util.UUID;

/**
 * Represents an immutable snapshot of a location.
 */
public class LocationSnapshot {

    public final UUID world;
    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    /**
     * Represents an immutable snapshot of a location.
     *
     * @param location Location to be snapshot
     */
    public LocationSnapshot(Location location) {
        if (location.getWorld() == null) {
            throw new IllegalArgumentException("Location has no world!");
        }

        this.world = location.getWorld().getUID();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    /**
     * Represents an immutable snapshot of a location.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     */
    public LocationSnapshot(UUID world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Transform back into a location.
     *
     * @return Location
     */
    public Location toLocation() {
        return new Location(Bukkit.getWorld(this.world), x, y, z, yaw, pitch);
    }

    /**
     * Retrieve distance between this snapshot and another
     * location.
     *
     * @param other The other location.
     * @return Squared distance.
     */
    public double distSq(Entity other) {
        return distSq(other.getLocation());
    }

    /**
     * Retrieve distance between this snapshot and another
     * location.
     *
     * @param other The other location.
     * @return Squared distance.
     */
    public double distSq(Location other) {
        if (other.getWorld() == null) {
            return Double.MAX_VALUE;
        } else if (!this.world.equals(other.getWorld().getUID())) {
            return Double.MAX_VALUE;
        } else {
            return NumberConversions.square(x - other.getX()) + NumberConversions.square(y - other.getY()) + NumberConversions.square(z - other.getZ());
        }
    }

    /**
     * Retrieve distance between this snapshot and another
     * location.
     *
     * @param other The other location.
     * @return Squared distance.
     */
    public double distSq(LocationSnapshot other) {
        if (!this.world.equals(other.world)) {
            return Double.MAX_VALUE;
        } else {
            return NumberConversions.square(x - other.x) + NumberConversions.square(y - other.y) + NumberConversions.square(z - other.z);
        }
    }
}
