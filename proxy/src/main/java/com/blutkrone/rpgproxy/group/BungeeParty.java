package com.blutkrone.rpgproxy.group;

import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BungeeParty {

    private BungeeGroupHandler handler;

    private String id;
    private UUID leader;
    private Set<UUID> members = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private int lonely_counter;

    BungeeParty(BungeeGroupHandler handler, String id, List<ProxiedPlayer> players) {
        this.handler = handler;
        this.id = id;
        this.lonely_counter = 0;
        for (ProxiedPlayer player : players) {
            this.members.add(player.getUniqueId());
            if (this.leader == null) {
                this.leader = player.getUniqueId();
            }
        }
    }

    BungeeParty(BungeeGroupHandler handler, String id, ProxiedPlayer player) {
        this.handler = handler;
        this.id = id;

        this.members.add(player.getUniqueId());
        if (this.leader == null) {
            this.leader = player.getUniqueId();
        }
    }

    /**
     * A party is considered lonely if it has less-equal one member, this
     * should only happen if someone wanted to party up but the other side
     * didn't accept the invitation
     *
     * @return How many consecutive times the party is considered lonely.
     */
    public int getLonelyCounter() {
        if (this.size() > 1) {
            this.lonely_counter = 0;
        }

        return ++this.lonely_counter;
    }

    /**
     * Broadcast the current state of the party to every server which
     * has players active.
     */
    public void broadcast() {
        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_PARTY_UPDATE_ONE);
        dump(composed);
        byte[] data = composed.toByteArray();
        this.handler.getPlugin().getProxy().getServers().forEach((id, server) -> {
            server.sendData(BungeeTable.CHANNEL_BUNGEE, data, false);
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
     * Count how many members are in this party.
     *
     * @return How many members we have
     */
    public int size() {
        return this.members.size();
    }

    /**
     * Add a member to the party.
     *
     * @param player Who to add
     */
    public void addPlayer(ProxiedPlayer player) {
        this.members.add(player.getUniqueId());
    }

    /**
     * Remove a player from the party, do note that if the party
     * has only one player left it should dissolve.
     * <p>
     * Should it be the leader who is leaving, the oldest player
     * in the party is to be promoted.
     *
     * @param player Who to remove
     */
    public void removePlayer(UUID player) {
        this.members.remove(player);
        if (this.leader.equals(player) && !this.members.isEmpty()) {
            this.leader = this.members.iterator().next();
        }
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
