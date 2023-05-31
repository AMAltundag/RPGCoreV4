package me.blutkrone.rpgcore.dungeon;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
     * Retrieve players in the dungeon.
     *
     * @param active Filter down to only target-able players
     * @return Players in dungeon
     */
    default List<Player> getPlayers(boolean active) {
        List<Player> players = getWorld().getPlayers();
        if (active) {
            players = new ArrayList<>(players);
            players.removeIf(player -> {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                return core_player == null || !core_player.isAllowTarget();
            });
        }
        return players;
    }

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
