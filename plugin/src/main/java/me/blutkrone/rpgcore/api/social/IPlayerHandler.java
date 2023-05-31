package me.blutkrone.rpgcore.api.social;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.List;

public interface IPlayerHandler {

    /**
     * A list of all players connected, this may offer players that
     * are connected on other servers on a network.
     *
     * @return connected players
     */
    List<String> getConnectedPlayers();

    /**
     * Broadcast a message across RPGCore.
     *
     * @param player Who sends the message
     * @param channel Chat channel we are using
     * @param message Message to deploy
     */
    void talk(Player player, String channel, BaseComponent[] message);
}
