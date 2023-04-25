package me.blutkrone.rpgcore.social.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.social.IPlayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BungeePlayerHandler implements IPlayerHandler, PluginMessageListener {

    private final Object SYNC = new Object();
    private String[] players = new String[0];

    public BungeePlayerHandler() {
        RPGCore plugin = RPGCore.inst();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();
            if (iterator.hasNext()) {
                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                data.writeUTF("PlayerList");
                data.writeUTF("ALL");
                iterator.next().sendPluginMessage(RPGCore.inst(), "BungeeCord", data.toByteArray());
            }
        }, 1, 100);
    }

    @Override
    public List<String> getConnectedPlayers() {
        synchronized (SYNC) {
            return Arrays.asList(this.players);
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equals("PlayerList")) {
            String server = in.readUTF();
            if (server.equalsIgnoreCase("ALL")) {
                String[] playerList = in.readUTF().split(", ");
                synchronized (SYNC) {
                    this.players = playerList;
                }
            }
        }
    }
}
