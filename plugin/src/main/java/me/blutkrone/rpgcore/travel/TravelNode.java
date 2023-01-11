package me.blutkrone.rpgcore.travel;

import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Location;

import java.util.function.Supplier;

public class TravelNode {

    // destination we will travel to (only if relevant)
    private Supplier<Location> where;
    // distance that activates node (-1 for auto-activate)
    private double activation_distance;
    // tooltip items on the node
    private String lc_item_info;

    public TravelNode(ConfigWrapper config) {
        this.where = config.getLazyLocation("position");
        this.activation_distance = config.getDouble("activation-distance");
        this.lc_item_info = config.getString("tooltip-item");
    }
}
