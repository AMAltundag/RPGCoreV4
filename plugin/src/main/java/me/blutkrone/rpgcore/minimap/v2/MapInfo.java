package me.blutkrone.rpgcore.minimap.v2;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Location;

import java.util.List;
import java.util.function.Supplier;

/**
 * Information necessary to render a map.
 */
public class MapInfo {
    // the menu to be rendered
    public final String menu;
    // the actions to be offered
    public final List<MapAction> actions;

    /**
     * Information necessary to render a map.
     *
     * @param config
     */
    public MapInfo(ConfigWrapper config) {
        this.menu = config.getString("menu");
        this.actions = config.getObjectList("actions", MapAction::new);
    }

    /**
     * A clickable action on the map, which can either perform
     * an action with the location or link to the next map.
     */
    public static class MapAction {
        // what slots to apply action to
        public final List<Integer> slots;
        // next map to be shown
        public final String next_map;
        // location to finalize into
        public final Supplier<Location> location;
        // tooltip to be shown
        public final String tooltip;

        public MapAction(ConfigWrapper config) {
            this.slots = config.getIntegerList("slots");
            this.next_map = config.getString("next-map", "");
            this.location = config.getLazyLocation("location");
            this.tooltip = config.getString("tooltip", "NOTHINGNESS");
        }
    }
}