package me.blutkrone.rpgcore.minimap.v2;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An anchor that represents a position on the map, this
 * is primarily used if a player opens the minimap from
 * their menu.
 */
public class MapAnchor {
    public List<String> path = new ArrayList<>();
    public Supplier<Location> position;

    public MapAnchor(ConfigWrapper config) {
        this.path = config.getStringList("path");
        this.position = config.getLazyLocation("location");
    }

    /**
     * The distance between the player and this anchor, a large
     * value is passed if not in the same world.
     *
     * @param player Who to check against
     * @return distance or Double.MAX_VALUE
     */
    public double distance(Player player) {
        try {
            Location position = this.position.get();
            if (player.getWorld() != position.getWorld())
                return Double.MAX_VALUE;
            return player.getLocation().distance(position);
        } catch (Exception ignored) {
            return Double.MAX_VALUE;
        }
    }
}
