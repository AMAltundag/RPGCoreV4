package me.blutkrone.rpgcore.minimap;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Location;

/**
 * A marker to show on the minimap, indicating a certain
 * point-of-interest.
 */
public class MapMarker {
    // where the marker is located at
    public final Location location;
    // which marker identifier is used
    public final String marker;
    // distance the marker is shown
    public final double distance;

    public MapMarker(ConfigWrapper section) {
        this.location = section.getLocation("location");
        this.distance = section.getDouble("distance");
        this.marker = section.getString("marker");
    }
}
