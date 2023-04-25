package com.blutkrone.rpgproxy.group;

import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

public class BungeeParty {

    private BungeeGroupHandler handler;

    private String id;
    private UUID leader;
    private Set<UUID> members = new HashSet<>();

    BungeeParty(BungeeGroupHandler handler, String id) {
        this.handler = handler;
        this.id = id;
    }

    /**
     * Broadcast the current state of the party to every server which
     * has players active.
     */
    public void broadcast() {
        ByteArrayDataOutput update = ByteStreams.newDataOutput();
        update.writeUTF(BungeeTable.PROXY_PARTY_UPDATE_ONE);
        dump(update);
        byte[] data = update.toByteArray();
        this.handler.getPlugin().getProxy().getServers().forEach((id, server) -> {
            server.sendData(BungeeTable.CHANNEL_RPGCORE, data, false);
        });
    }

    /**
     * Dump data into the given data input.
     *
     * @param data the data object to dump into.
     */
    public void dump(ByteArrayDataOutput data) {
        data.writeUTF(this.id);
        data.writeUTF(this.leader.toString());
        data.writeInt(this.members.size());
        for (UUID member : this.members) {
            data.writeUTF(member.toString());
        }
    }

    /**
     * Leader of the party, but as a Proxy Player instance..
     *
     * @return Who leads the party.
     */
    public ProxiedPlayer getLeader() {
        return handler.getPlugin().getProxy().getPlayer(this.leader);
    }

    /**
     * Update who leads the party.
     *
     * @param leader Who is the party leader.
     */
    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    /**
     * Check if player is the leader of the party.
     *
     * @param player Who wants to know if they are the leader.
     * @return Player is the leader of this party.
     */
    public boolean isLeader(ProxiedPlayer player) {
        return player != null && player.getUniqueId().equals(this.leader);
    }

    /**
     * Check if player is the leader of the party.
     *
     * @param player Who wants to know if they are the leader.
     * @return Player is the leader of this party.
     */
    public boolean isLeader(UUID player) {
        return player != null && player.equals(this.leader);
    }

    /**
     * Check if player is a member of the party.
     *
     * @param player Who wants to know if they are a member.
     * @return Player who wants to know if they are a member.
     */
    public boolean isMember(ProxiedPlayer player) {
        return this.members.contains(player.getUniqueId());
    }

    /**
     * Check if player is a member of the party.
     *
     * @param player Who wants to know if they are a member.
     * @return Player who wants to know if they are a member.
     */
    public boolean isMember(UUID player) {
        return this.members.contains(player);
    }

    /**
     * Fetch unique identifier for player.
     *
     * @return Distinct identifier of this party.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Fetch all players registered to the party.
     *
     * @return UUID of every party member.
     */
    public Set<UUID> getMembersUUID() {
        return this.members;
    }

    /**
     * Fetch all players connected to the proxy.
     *
     * @return Members also connected to the proxy.
     */
    public List<ProxiedPlayer> getMembersOnline() {
        List<ProxiedPlayer> players = new ArrayList<>();
        for (UUID member : this.members) {
            ProxiedPlayer player = handler.getPlugin().getProxy().getPlayer(member);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }
}
