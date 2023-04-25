package me.blutkrone.rpgcore.api.bungee;

import com.google.common.io.ByteArrayDataInput;
import me.blutkrone.rpgcore.RPGCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Common interface for everything that performs bungee handling.
 */
public interface IBungeeHandling {

    /**
     * Incoming message from the bungee channel.
     *
     * @param recipient Who received the message
     * @param channel   What (rpgcore) sub channel
     * @param data      Data in the message
     */
    void onBungeeMessage(Player recipient, String channel, ByteArrayDataInput data);

    /**
     * Perform a synchronous task, this is mainly to synchronize out of
     * a bungee message.
     *
     * @param runnable what to execute
     */
    default void doSyncTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(RPGCore.inst(), runnable);
    }
}
