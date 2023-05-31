package com.blutkrone.rpgproxy.player;

import com.blutkrone.rpgproxy.RPGProxy;
import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BungeePlayerHandler implements Listener {

    private static final Object SYNC = new Object();

    private final RPGProxy plugin;
    private final Set<String> active_users = new HashSet<>();
    private boolean active_dirty = false;

    public BungeePlayerHandler(RPGProxy plugin) {
        this.plugin = plugin;
        this.plugin.getProxy().getPluginManager().registerListener(plugin, this);
        this.plugin.getProxy().getScheduler().schedule(plugin, () -> {
            // do nothing while players are absent
            if (plugin.getProxy().getPlayers().isEmpty()) {
                return;
            }
            // threaded check to ensure we have an update
            List<String> snapshot;
            synchronized (SYNC) {
                if (!this.active_dirty) {
                    return;
                }
                snapshot = new ArrayList<>(this.active_users);
                this.active_dirty = false;
            }
            // dispatch to every server with a player (no buffer)
            ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_LIST_PLAYER);
            composed.writeInt(snapshot.size());
            snapshot.forEach(composed::writeUTF);
            for (ServerInfo server : plugin.getProxy().getServers().values()) {
                server.sendData(BungeeTable.CHANNEL_BUNGEE, composed.toByteArray(), false);
            }
        }, 0L, 5L, TimeUnit.SECONDS);
    }

    /**
     * Build a list of all users currently online.
     *
     * @return List of users online.
     */
    public byte[] getDataForFullUsersUpdate() {
        List<String> snapshot;
        synchronized (SYNC) {
            snapshot = new ArrayList<>(this.active_users);
        }
        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_LIST_PLAYER);
        composed.writeInt(snapshot.size());
        snapshot.forEach(composed::writeUTF);
        return composed.toByteArray();
    }

    /**
     * Proxy plugin we are instantiated by
     *
     * @return Proxy Plugin
     */
    public RPGProxy getPlugin() {
        return plugin;
    }

    /**
     * Process messages specific to the group handler.
     *
     * @param whoAsked Who was the message sent to.
     * @param channel  What channel was the message sent to.
     * @param data     What data did it contain.
     * @return Whether it was parsed by this handler
     */
    public boolean read(ProxiedPlayer whoAsked, String channel, ByteArrayDataInput data) {
        if (BungeeTable.PROXY_BOUND_DELIVER_CHAT.equals(channel)) {
            // rout a message to the appropriate recipients
            String chat_channel = data.readUTF();
            BaseComponent[] message = ComponentSerializer.parse(data.readUTF());

            if (chat_channel.startsWith("@")) {
                ProxiedPlayer target = getPlugin().getProxy().getPlayer(chat_channel.substring(1));
                if (target != null) {
                    target.sendMessage(ChatMessageType.SYSTEM, message);
                    whoAsked.sendMessage(ChatMessageType.SYSTEM, message);
                } else {
                    getPlugin().sendTranslatedMessage(whoAsked, "chat_whisper_not_found", chat_channel.substring(1));
                }
            } else if (chat_channel.equals("party")) {
                List<ProxiedPlayer> party = getPlugin().getMatchHandler().getPartyMembers(whoAsked);
                if (!party.isEmpty()) {
                    for (ProxiedPlayer target : party) {
                        target.sendMessage(ChatMessageType.SYSTEM, message);
                    }
                } else {
                    getPlugin().sendTranslatedMessage(whoAsked, "chat_you_have_no_party");
                }
            } else {
                getPlugin().getProxy().getScheduler().runAsync(getPlugin(), () -> {
                    for (ProxiedPlayer target : getPlugin().getProxy().getPlayers()) {
                        target.sendMessage(ChatMessageType.SYSTEM, message);
                    }
                });
            }

            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onJoin(PlayerDisconnectEvent event) {
        synchronized (SYNC) {
            this.active_users.add(event.getPlayer().getName());
            this.active_dirty = true;
        }
    }

    @EventHandler
    public void onQuit(PostLoginEvent event) {
        synchronized (SYNC) {
            this.active_users.remove(event.getPlayer().getName());
            this.active_dirty = true;
        }
    }
}
