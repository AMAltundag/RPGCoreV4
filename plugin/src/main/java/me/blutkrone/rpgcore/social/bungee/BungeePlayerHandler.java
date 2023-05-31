package me.blutkrone.rpgcore.social.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.bungee.IBungeeHandling;
import me.blutkrone.rpgcore.api.social.IPlayerHandler;
import me.blutkrone.rpgcore.language.LanguageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BungeePlayerHandler implements IPlayerHandler, IBungeeHandling {

    private final Object SYNC = new Object();
    private String[] players = new String[0];

    public BungeePlayerHandler() {
    }

    @Override
    public List<String> getConnectedPlayers() {
        synchronized (SYNC) {
            return Arrays.asList(this.players);
        }
    }

    @Override
    public void talk(Player player, String channel, BaseComponent[] message) {
        // deploy to proxy for message routing
        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.PROXY_BOUND_DELIVER_CHAT);
        composed.writeUTF(channel);
        composed.writeUTF(ComponentSerializer.toString(message));
        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, composed.toByteArray());
    }

    @Override
    public void onBungeeMessage(Player recipient, String channel, ByteArrayDataInput data) {
        if (channel.equalsIgnoreCase(BungeeTable.SERVER_BOUND_BASIC_MESSAGE)) {
            // proxy wants to deploy message
            String proxy_message = data.readUTF();
            // grab LC information
            LanguageManager language = RPGCore.inst().getLanguageManager();
            String translation = language.getTranslation(proxy_message);
            int size = data.readInt();
            for (int i = 0; i < size; i++) {
                String arg = data.readUTF();
                translation = translation.replace("{" + i + "}", arg);
            }
            // send message to player
            recipient.sendMessage(translation);
        } else if (channel.equalsIgnoreCase(BungeeTable.SERVER_BOUND_LIST_PLAYER)) {
            // proxy wants to inform about player list
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                int size = data.readInt();
                String[] players = new String[size];
                for (int i = 0; i < size; i++) {
                    players[i] = data.readUTF();
                }
                synchronized (SYNC) {
                    this.players = players;
                }
            });
        }
    }
}
