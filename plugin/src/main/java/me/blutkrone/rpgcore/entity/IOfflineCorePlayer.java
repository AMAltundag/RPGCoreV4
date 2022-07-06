package me.blutkrone.rpgcore.entity;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * A common interface which allows to track offline players.
 */
public interface IOfflineCorePlayer {
    /**
     * Fetch the name the player was last known by.
     *
     * @return the name of the player.
     */
    String getName();

    /**
     * Fetch the bukkit offline player instance.
     *
     * @return a bukkit offline player instance.
     */
    OfflinePlayer getOfflinePlayer();

    /**
     * Unique ID the player was last known by.
     *
     * @return the unique player identifier.
     */
    UUID getUniqueId();
}
