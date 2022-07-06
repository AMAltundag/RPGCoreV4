package me.blutkrone.rpgcore.api;

import org.bukkit.Location;

/**
 * An origin has a location associated with it, primarily
 * used by the skill system to have a uniform interface to
 * contain locations.
 */
public interface IOrigin {
    /**
     * The implementing class transformed into a location.
     *
     * @return the location of this origin.
     */
    Location getLocation();
}
