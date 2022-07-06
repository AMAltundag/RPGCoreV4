package me.blutkrone.rpgcore.minimap;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.world.ChunkIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * A region which is associated with a specific minimap.
 */
public class MapRegion {
    // where the region starts
    public final int x1, y1, z1;
    // where the region ends
    public final int x2, y2, z2;
    // which world the region is active
    public final String world;
    // which map is rendered inside
    public final String map;

    public MapRegion(String map, ConfigWrapper config) {
        this.map = map;
        this.world = config.getString("world");
        Vector v1 = config.getVector("start");
        Vector v2 = config.getVector("finish");
        x1 = Math.min(v1.getBlockX(), v2.getBlockX());
        y1 = Math.min(v1.getBlockY(), v2.getBlockY());
        z1 = Math.min(v1.getBlockZ(), v2.getBlockZ());
        x2 = Math.max(v1.getBlockX(), v2.getBlockX());
        y2 = Math.max(v1.getBlockY(), v2.getBlockY());
        z2 = Math.max(v1.getBlockZ(), v2.getBlockZ());
    }

    /**
     * Checks if a given location is within this map region.
     *
     * @param where the location we are checking
     * @return true if we are contained
     */
    public boolean contains(Location where) {
        // check if the location is in here
        if (!this.world.equalsIgnoreCase(where.getWorld().getName()))
            return false;
        // quicker to access the relevant parameters
        int cX = where.getBlockX();
        int cY = where.getBlockY();
        int cZ = where.getBlockZ();
        // ensure the position is within threshold
        return cX >= x1 && cX <= x2
                && cY >= y1 && cY <= y2
                && cZ >= z1 && cZ <= z2;
    }

    /**
     * Checks if a given chunk is within this map region.
     *
     * @param where the chunk we are checking
     * @return true if we are contained
     */
    public boolean contains(ChunkIdentifier where) {
        // check if the location is in here
        World world = Bukkit.getWorld(this.world);
        if (where == null || !world.getUID().equals(where.getWorld()))
            return false;
        // reduce thresholds into chunk positions
        int x1 = this.x1 >> 4;
        int x2 = this.x2 >> 4;
        int z1 = this.z1 >> 4;
        int z2 = this.z2 >> 4;
        int cX = where.getX();
        int cZ = where.getZ();
        return cX >= x1 && cX <= x2 && cZ >= z1 && cZ <= z2;
    }

    @Override
    public String toString() {
        return String.format("MapRegion{map=%s;x1=%s;x2=%s;y1=%s;y2=%s;z1=%s;z2=%s;world=%s}",
                map, x1, x2, y1, y2, z1, z2, world);
    }
}
