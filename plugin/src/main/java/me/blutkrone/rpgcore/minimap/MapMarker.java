package me.blutkrone.rpgcore.minimap;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Location;

import java.util.function.Supplier;

/**
 * A marker to show on the minimap, indicating a certain
 * point-of-interest.
 */
public class MapMarker {
    // where the marker is located at
    private Supplier<Location> where;
    private Location where_cached;
    // which marker identifier is used
    public final String marker;
    // distance the marker is shown
    public final double distance;

    public MapMarker(ConfigWrapper section) {
        this.where = section.getLazyLocation("location");
        this.marker = section.getString("marker");
        this.distance = section.getDouble("distance");
    }

    public MapMarker(Location location, String marker, double distance) {
        this.where = () -> location;
        this.marker = marker;
        this.distance = distance;
    }

    public Location getLocation() {
        if (this.where_cached == null) {
            this.where_cached = this.where.get();
            this.where = null;
        }

        return this.where_cached;
    }
}
