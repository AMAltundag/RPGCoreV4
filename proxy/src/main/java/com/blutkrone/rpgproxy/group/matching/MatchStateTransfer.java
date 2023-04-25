package com.blutkrone.rpgproxy.group.matching;

import com.blutkrone.rpgproxy.group.BungeeGroupHandler;
import com.blutkrone.rpgproxy.group.BungeeParty;
import com.blutkrone.rpgproxy.util.BungeeConfig;
import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

import java.util.*;
import java.util.stream.Collectors;

/*
 * Prepare to depart from target servers
 */
public class MatchStateTransfer implements IMatchPhase {

    final BungeeGroupHandler handler;

    final UUID id;
    final String content;
    final Set<UUID> interested;
    final Set<UUID> ready;

    final BungeeParty pre_grouped;

    MatchStateTransfer(BungeeGroupHandler handler, MatchStateVerify before) {
        this.handler = handler;
        this.id = before.id;
        this.content = before.content;
        this.interested = before.interested;
        this.pre_grouped = null;
        this.ready = new HashSet<>();
    }

    public MatchStateTransfer(BungeeGroupHandler handler, BungeeParty party, String content) {
        this.handler = handler;
        this.id = UUID.randomUUID();
        this.content = content;
        this.interested = new HashSet<>(party.getMembersUUID());
        this.pre_grouped = party;
        this.ready = new HashSet<>();
    }

    @Override
    public UUID getUniqueID() {
        return this.id;
    }

    @Override
    public Collection<UUID> getInvolved() {
        return this.interested;
    }

    @Override
    public IMatchPhase positive(ProxiedPlayer player) {
        // await transfer readiness from everyone
        this.ready.add(player.getUniqueId());
        if (this.ready.size() < this.interested.size()) {
            return this;
        }
        // finalize the matching process
        finish();
        return null;
    }

    @Override
    public IMatchPhase negative(ProxiedPlayer player) {
        // if we fail, we simply do not participate
        this.interested.remove(player.getUniqueId());
        // if in a party, we get kicked out
        if (this.pre_grouped != null) {
            this.handler.forceQuitParty(player);
        }
        // check if we created a ready environment
        if (this.ready.size() >= this.interested.size()) {
            finish();
            return null;
        }
        // do not perform any change at all
        return this;
    }

    /*
     * Matching process has concluded, we have a group of
     * players ready to engage in the same kind of content
     */
    private void finish() {
        final BungeeConfig.MatchMakerInfo content_info = this.handler.getPlugin().getConfig().matchmaker.get(content);

        // if nobody remained, fail silently
        if (this.interested.isEmpty()) {
            return;
        }

        // find lowest population content server to use
        ServerInfo wanted = null;
        for (ServerInfo server : this.handler.getPlugin().getProxy().getServers().values()) {
            if (!content_info.servers.contains(server.getName())) {
                continue;
            }
            if (server.getPlayers().size() + interested.size() > content_info.maximum_players) {
                continue;
            }
            if (wanted == null || wanted.getPlayers().size() > server.getPlayers().size()) {
                wanted = server;
            }
        }

        // warn if no content server can be utilized
        if (wanted == null) {
            ByteArrayDataOutput data = ByteStreams.newDataOutput();
            data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
            data.writeUTF(BungeeTable.PROXY_MATCH_TRANSFER_ERROR);
            byte[] bytes = data.toByteArray();
            for (UUID uuid : getInvolved()) {
                this.handler.getPlugin().sendData(uuid, bytes);
            }
            return;
        }

        // create the content instance we want
        String party_id;
        List<ProxiedPlayer> players;

        if (pre_grouped == null) {
            party_id = "matchmaker_" + this.content + "_" + BungeeGroupHandler.PARTY_ID_COUNTER++;
            players = this.interested.stream()
                    .map(uuid -> this.handler.getPlugin().getProxy().getPlayer(uuid))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            this.handler.forceParty(party_id, players);
        } else {
            party_id = pre_grouped.getId();
            players = pre_grouped.getMembersOnline();
        }

        // deploy the players over to the server (or inform about failing to do so)
        for (ProxiedPlayer player : players) {
            if (player.getServer().getInfo() != wanted) {
                player.connect(wanted, (response, throwable) -> {
                    // allow them to re-connect
                    ByteArrayDataOutput data = ByteStreams.newDataOutput();
                    data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                    data.writeUTF(BungeeTable.PROXY_MATCH_TRANSFER_ERROR);
                    player.sendData(BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
                    // force player out of the party we've matched
                    this.handler.forceQuitParty(player);
                }, ServerConnectEvent.Reason.PLUGIN);
            }
        }

        // queue packet that will create our content instance
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.PROXY_MATCH_TRANSFER_ARRIVE);
        data.writeUTF(party_id);
        data.writeInt(players.size());
        for (ProxiedPlayer player : players) {
            data.writeUTF(player.getName());
        }
        data.writeUTF(content);
        wanted.sendData(BungeeTable.CHANNEL_BUNGEE, data.toByteArray(), true);
    }
}
