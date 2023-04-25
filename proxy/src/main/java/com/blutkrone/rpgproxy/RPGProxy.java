package com.blutkrone.rpgproxy;

import com.blutkrone.rpgproxy.group.BungeeGroupHandler;
import com.blutkrone.rpgproxy.util.BungeeConfig;
import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class RPGProxy extends Plugin implements Listener {

    private BungeeConfig bungee_config;
    private BungeeGroupHandler group_handler;

    private Map<String, UUID> name_to_uuid = new HashMap<>();

    @Override
    public void onEnable() {
        this.getProxy().registerChannel(BungeeTable.CHANNEL_RPGCORE);
        this.getProxy().getPluginManager().registerListener(this, this);

        this.bungee_config = createConfig();
        this.group_handler = new BungeeGroupHandler(this);

        getProxy().getScheduler().schedule(this, () -> {
            // flush the cache if too much data in it
            if (name_to_uuid.size() > getProxy().getConfig().getPlayerLimit() * 10) {
                name_to_uuid.clear();
                for (ProxiedPlayer online : getProxy().getPlayers()) {
                    name_to_uuid.put(online.getName(), online.getUniqueId());
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Retrieve the last known UUID in this session, this is NOT tracked
     * permanently and may lose reference at any point in time.
     *
     * @param name How was the player called
     * @return The last UUID we know them by
     */
    public UUID getLastKnownUUID(String name) {
        return name_to_uuid.get(name);
    }

    /**
     * Deploy a RPGCore packet to the target player.
     *
     * @param target Who to deploy the data to.
     * @param data   Data to be deployed.
     */
    public void sendData(UUID target, byte[] data) {
        ProxiedPlayer player = getProxy().getPlayer(target);
        if (player != null) {
            player.sendData(BungeeTable.CHANNEL_BUNGEE, data);
        }
    }

    /**
     * Deploy a RPGCore packet to the target player.
     *
     * @param target Who to deploy the data to.
     * @param data   Data to be deployed.
     */
    public void sendData(ProxiedPlayer target, byte[] data) {
        target.sendData(BungeeTable.CHANNEL_BUNGEE, data);
    }

    /**
     * Response to a request filed against the proxy, this is only intended
     * for very simplistic communication protocol.
     *
     * @param player  Who filed the request.
     * @param message Response to the request.
     */
    public void sendTranslatedMessage(ProxiedPlayer player, String message, String... args) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.PROXY_BASIC_MESSAGE);
        data.writeUTF(message);
        data.writeInt(args.length);
        for (String arg : args) {
            data.writeUTF(arg);
        }
        player.sendData(BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
    }

    /**
     * Handler responsible for group play.
     *
     * @return Group play handler.
     */
    public BungeeGroupHandler getMatchHandler() {
        return group_handler;
    }

    /**
     * Configuration dedicated to the bungee server.
     *
     * @return Bungee configuration
     */
    public BungeeConfig getConfig() {
        return bungee_config;
    }

    @EventHandler
    public void onReceiveMessageFromServer(PluginMessageEvent event) {
        // only process rpgcore
        if (!event.getTag().equals(BungeeTable.CHANNEL_RPGCORE)) {
            return;
        }
        // rpgcore has no protocol outgoing for players
        event.setCancelled(true);
        // ensure message is sent from a spigot server
        if (!(event.getSender() instanceof Server)) {
            return;
        }
        // ensure message is targeted at a specific player
        if (!(event.getReceiver() instanceof ProxiedPlayer)) {
            return;
        }
        // process our request
        ProxiedPlayer receiver = (ProxiedPlayer) event.getReceiver();
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String channel = in.readUTF();
        // allow match handler to process message
        if (getMatchHandler().read(receiver, channel, in)) {
            return;
        }
    }

    @EventHandler
    public void onFixOutOfSyncPartyView(ServerSwitchEvent event) {
        // if we are the first player, the server may be out-of-sync
        int size = event.getPlayer().getServer().getInfo().getPlayers().size();
        if (size == 1) {
            event.getPlayer().sendData(BungeeTable.CHANNEL_RPGCORE, getMatchHandler().getDataForFullPartyUpdate());
        }
    }

    @EventHandler
    public void onTrackNameToUUID(PostLoginEvent event) {
        // track name to uuid for expiration
        this.name_to_uuid.put(event.getPlayer().getName(), event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuitServer(PlayerDisconnectEvent event) {
        // quit queue and leave party on disconnect
        this.getMatchHandler().terminateMatching(event.getPlayer());
        this.getMatchHandler().forceQuitParty(event.getPlayer());
    }

    private BungeeConfig createConfig() {
        try {
            createDefaultConfig();
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            return new BungeeConfig(configuration);
        } catch (IOException e) {
            e.printStackTrace();
            return new BungeeConfig();
        }
    }

    /*
     * Should we have no configuration file, we write the default
     * configuration file.
     *
     * @throws IOException
     */
    private void createDefaultConfig() throws IOException {
        // create absent folder structure
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        // ensure that we have no config file
        File file = new File(getDataFolder(), "config.yml");
        if (file.exists()) {
            return;
        }
        // write default bundled configuration
        try (InputStream in = getResourceAsStream("config.yml")) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
