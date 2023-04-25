package me.blutkrone.rpgcore.dungeon;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public interface IDungeonInstance {

    /**
     * Template to base dungeon off.
     *
     * @return Template to use.
     */
    CoreDungeon getTemplate();

    /**
     * Retrieve the checkpoint for a player, should they not have a
     * checkpoint the entrance location is expected.
     *
     * @param player Whose to check
     * @return Entry position for player
     */
    Location getCheckpoint(Player player);

    /**
     * World the instance is backed by.
     *
     * @return World backing the instance
     */
    World getWorld();

    /**
     * Handle the tick loop of the instance.
     *
     * @return Whether instance can be abandoned.
     */
    boolean update();

    /**
     * Invite players by their name, this can timeout, will
     * add players to the dungeon as they join.
     *
     * @param players Who wants to join.
     */
    void invite(List<String> players);
}
