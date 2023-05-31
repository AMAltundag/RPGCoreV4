package com.blutkrone.rpgproxy;

import com.blutkrone.rpgproxy.group.BungeeGroupHandler;
import com.blutkrone.rpgproxy.player.BungeePlayerHandler;
import com.blutkrone.rpgproxy.util.BungeeConfig;
import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class RPGProxy extends Plugin implements Listener {

    private BungeeConfig bungee_config;
    private BungeeGroupHandler group_handler;
    private BungeePlayerHandler player_handler;

    private Map<String, UUID> name_to_uuid = new ConcurrentHashMap<>();
    private Map<UUID, String> uuid_to_name = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        this.getProxy().registerChannel(BungeeTable.CHANNEL_RPGCORE);
        this.getProxy().getPluginManager().registerListener(this, this);

        this.bungee_config = createConfig();
        this.group_handler = new BungeeGroupHandler(this);
        this.player_handler = new BungeePlayerHandler(this);

        getProxy().getScheduler().schedule(this, () -> {
            // flush the cache if too much data in it
            if (name_to_uuid.size() > getProxy().getConfig().getPlayerLimit() * 10) {
                name_to_uuid.clear();
                for (ProxiedPlayer online : getProxy().getPlayers()) {
                    name_to_uuid.put(online.getName(), online.getUniqueId());
                }
            }
            // flush the cache if too much data in it
            if (uuid_to_name.size() > getProxy().getConfig().getPlayerLimit() * 10) {
                uuid_to_name.clear();
                for (ProxiedPlayer online : getProxy().getPlayers()) {
                    uuid_to_name.put(online.getUniqueId(), online.getName());
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Retrieve the server with the least number of players which
     * runs fine on the given content.
     *
     * @param content What content to check.
     * @param players How many players want to join.
     */
    public ServerInfo getBestServerFor(String content, int players) {
        // check the configuration for the content
        final BungeeConfig.MatchMakerInfo content_info = getConfig().matchmaker.get(content);
        if (content_info == null) {
            getProxy().getLogger().severe("Could not find matchmaker info for: " + content);
            return null;
        }
        // find lowest population content server to use
        ServerInfo wanted = null;
        for (ServerInfo server : this.getProxy().getServers().values()) {
            if (server.isRestricted()) {
                getProxy().getLogger().severe("skip: server is restricted");
                continue;
            }
            if (!content_info.servers.contains(server.getName())) {
                getProxy().getLogger().severe("skip: have %s want %s".formatted(server.getName(), content_info.servers));
                continue;
            }
            if (server.getPlayers().size() + players > content_info.maximum_players) {
                getProxy().getLogger().severe("skip: server is overpopulated");
                continue;
            }
            if (wanted == null || wanted.getPlayers().size() > server.getPlayers().size()) {
                wanted = server;
            }
        }
        // offer up the server
        return wanted;
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
     * Retrieve the last known UUID in this session, this is NOT tracked
     * permanently and may lose reference at any point in time.
     *
     * @param name How was the player called
     * @return The last UUID we know them by
     */
    public String getLastKnownName(UUID name) {
        return uuid_to_name.get(name);
    }

    /**
     * Send a RPGCore packet to every server
     *
     * @param data What data to deploy
     */
    public void send(byte[] data) {
        for (ServerInfo server : this.getProxy().getServers().values()) {
            server.sendData(BungeeTable.CHANNEL_BUNGEE, data, false);
        }
    }

    /**
     * Send a RPGCore packet to every server
     *
     * @param data What data we are sending to the server
     * @param queue Hold the message for later sending if it cannot be sent immediately.
     */
    public void send(byte[] data, boolean queue) {
        for (ServerInfo server : this.getProxy().getServers().values()) {
            server.sendData(BungeeTable.CHANNEL_BUNGEE, data, queue);
        }
    }

    /**
     * Send a RPGCore packet to the target server
     *
     * @param server What server to send to
     * @param data What data to deploy
     */
    public void send(Server server, byte[] data) {
        server.getInfo().sendData(BungeeTable.CHANNEL_BUNGEE, data, false);
    }

    /**
     * Send a RPGCore packet to the target server
     *
     *
     *
     * @param server What server to send to
     * @param data What data we are sending to the server
     * @param queue Hold the message for later sending if it cannot be sent immediately.
     */
    public void send(Server server, byte[] data, boolean queue) {
        server.getInfo().sendData(BungeeTable.CHANNEL_BUNGEE, data, false);
    }

    /**
     * Send a RPGCore packet to the target server
     *
     * @param server What server to send to
     * @param data What data to deploy
     */
    public void send(ServerInfo server, byte[] data) {
        server.sendData(BungeeTable.CHANNEL_BUNGEE, data, false);
    }

    /**
     * Send a RPGCore packet to the target server
     *
     *
     *
     * @param server What server to send to
     * @param data What data we are sending to the server
     * @param queue Hold the message for later sending if it cannot be sent immediately.
     */
    public void send(ServerInfo server, byte[] data, boolean queue) {
        server.sendData(BungeeTable.CHANNEL_BUNGEE, data, false);
    }

    /**
     * Response to a request filed against the proxy, this is only intended
     * for very simplistic communication protocol.
     *
     * @param player  Who filed the request.
     * @param message Response to the request.
     */
    public void sendTranslatedMessage(ProxiedPlayer player, String message, String... args) {
        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_BASIC_MESSAGE, player);
        composed.writeUTF(message);
        composed.writeInt(args.length);
        for (String arg : args) {
            composed.writeUTF(arg);
        }
        send(player.getServer(), composed.toByteArray());
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
     * Handler responsible for player processing.
     *
     * @return Player handler
     */
    public BungeePlayerHandler getPlayerHandler() {
        return player_handler;
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
        // only process if addressed to RPGCore
        if (!event.getTag().equals(BungeeTable.CHANNEL_BUNGEE)) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String sub_channel = in.readUTF();
        if (!sub_channel.equals(BungeeTable.CHANNEL_RPGCORE)) {
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
        ProxiedPlayer whoAsked = (ProxiedPlayer) event.getReceiver();
        String channel = in.readUTF();
        // allow match handler to process message
        if (getMatchHandler().read(whoAsked, channel, in)) {
            return;
        }
        if (getPlayerHandler().read(whoAsked, channel, in)) {
            return;
        }
    }

    @EventHandler
    public void onFixOutOfSync(ServerSwitchEvent event) {
        // if we are the first player, the server may be out-of-sync
        int size = event.getPlayer().getServer().getInfo().getPlayers().size();
        if (size == 1) {
            this.send(event.getPlayer().getServer(), getMatchHandler().buildFullPartyInfo());
            this.send(event.getPlayer().getServer(), getMatchHandler().buildFullMatchInfo());
            this.send(event.getPlayer().getServer(), getPlayerHandler().getDataForFullUsersUpdate());
        }
    }

    @EventHandler
    public void onTrackNameToUUID(PostLoginEvent event) {
        // track name to uuid for expiration
        this.name_to_uuid.put(event.getPlayer().getName(), event.getPlayer().getUniqueId());
        this.uuid_to_name.put(event.getPlayer().getUniqueId(), event.getPlayer().getName());
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
