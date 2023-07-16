package com.blutkrone.rpgproxy.group;

import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A matchmaker that gathers players to run a dungeon, this is not a
 * thread-safe implementation and expects external synchronization.
 */
public final class Matching {

    // handler that instantiated the match
    private final BungeeGroupHandler handler;
    // the content which was selected to run
    private final String content;
    // unique identifier for match process
    private final UUID id;

    // everyone in the match process
    private Map<UUID, State> interested = new ConcurrentHashMap<>();
    // seconds until we have a timeout
    private int time_until_timeout;
    // current on-going phase
    private Phase phase;
    // what server was selected for this
    private ServerInfo server;
    // in case we were pre-grouped
    private String party_id;

    /**
     * Create a matching instance that will make an effort to
     * pool up a party.
     *
     * @param interested Everyone to ask
     */
    public Matching(BungeeGroupHandler handler, String content, List<ProxiedPlayer> interested) {
        this.handler = handler;
        this.content = content;
        this.id = UUID.randomUUID();
        this.time_until_timeout = 30;
        this.phase = Phase.ASKING;
        this.party_id = "matchmaker_" + this.content + "_" + BungeeGroupHandler.PARTY_ID_COUNTER++;

        for (ProxiedPlayer player : interested) {
            // initialize as match seeker
            this.interested.put(player.getUniqueId(), State.PENDING);
            // ask everyone if interested
            ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_ASK, player);
            composed.writeUTF(content);
            composed.writeInt(interested.size());
            for (ProxiedPlayer other : interested) {
                composed.writeUTF(other.getUniqueId().toString());
            }
            handler.getPlugin().send(player.getServer(), composed.toByteArray());
        }
    }

    /**
     * Create a matching instance that will make an effort to
     * pool up a party.
     *
     * @param party Party who wants to queue up.
     */
    public Matching(BungeeGroupHandler handler, String content, BungeeParty party) {
        this.handler = handler;
        this.content = content;
        this.id = UUID.randomUUID();
        this.time_until_timeout = 30;
        this.phase = Phase.ASKING;
        this.party_id = party.getId();

        // pre-grouped party needs no approval
        for (ProxiedPlayer player : party.getMembersOnline()) {
            this.interested.put(player.getUniqueId(), State.YES);
        }
    }

    /**
     * Lists everyone involved in the matching process, do note that
     * while this collection is concurrent (iE safe read-write access)
     * modification or stability requires external synchronization.
     *
     * @return UUID of everyone involved.
     */
    public Map<UUID, State> getInterested() {
        return interested;
    }

    /**
     * Check if a player is involved in this match.
     *
     * @param player Who to check
     * @return Are we involved
     */
    public boolean contains(ProxiedPlayer player) {
        return contains(player.getUniqueId());
    }

    /**
     * Check if a player is involved in this match.
     *
     * @param uuid Who to check
     * @return Are we involved
     */
    public boolean contains(UUID uuid) {
        State state = this.interested.get(uuid);
        return state != null && state != State.NO;
    }

    /**
     * The phase we are currently operating.
     *
     * @return Phase to operate.
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * Handle a response received for a certain user, ensure that the
     * response given is in context of {@link #getPhase()} and we are
     * one of the original interest holders.
     *
     * @param member   Whose response did we receive
     * @param response What response did we receive
     */
    public void response(UUID member, boolean response) {
        if (response) {
            this.interested.put(member, State.YES);
        } else {
            this.interested.put(member, State.NO);
        }
    }

    /**
     * Ticked processing of the match behaviour, ensure this
     * is deployed off the main-thread.
     *
     * @return True if we can terminate.
     */
    public boolean tick() {
        // without members, the match can terminate
        if (this.interested.isEmpty()) {
            return true;
        }

        // if we have a timeout, force a negative response
        if (--this.time_until_timeout == 0) {
            for (Map.Entry<UUID, State> entry : this.interested.entrySet()) {
                if (entry.getValue() == State.PENDING) {
                    entry.setValue(State.NO);
                }
            }
        }

        if (getPhase() == Phase.ASKING) {
            if (this.interested.containsValue(State.NO)) {
                // inform about matching failure (someone rejected)
                for (Map.Entry<UUID, State> entry : this.interested.entrySet()) {
                    ProxiedPlayer player = this.handler.getPlugin().getProxy().getPlayer(entry.getKey());
                    if (player != null && player.isConnected()) {
                        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_FAIL, player);
                        composed.writeUTF(this.id.toString());
                        composed.writeUTF("match_fail_player_rejected");
                        this.handler.getPlugin().send(player.getServer(), composed.toByteArray());
                    }
                }
                // take members out of match
                this.interested.clear();
            } else if (!this.interested.containsValue(State.PENDING)) {
                // ask servers to verify the players
                this.phase = Phase.VERIFY;
                this.interested.entrySet().forEach(entry -> entry.setValue(State.PENDING));
                this.time_until_timeout = 10;
                for (Map.Entry<UUID, State> entry : this.interested.entrySet()) {
                    ProxiedPlayer player = this.handler.getPlugin().getProxy().getPlayer(entry.getKey());
                    if (player != null && player.isConnected()) {
                        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_VERIFY, player);
                        composed.writeUTF(this.id.toString());
                        this.handler.getPlugin().send(player.getServer(), composed.toByteArray());
                    } else {
                        entry.setValue(State.NO);
                    }
                }
            }
        } else if (getPhase() == Phase.VERIFY) {
            if (this.interested.containsValue(State.NO)) {
                // inform about matching failure (someone couldn't verify)
                for (Map.Entry<UUID, State> entry : this.interested.entrySet()) {
                    ProxiedPlayer player = this.handler.getPlugin().getProxy().getPlayer(entry.getKey());
                    if (player != null && player.isConnected()) {
                        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_FAIL, player);
                        composed.writeUTF(this.id.toString());
                        composed.writeUTF("match_fail_player_not_ready");
                        this.handler.getPlugin().send(player.getServer(), composed.toByteArray());
                    }
                }
                // take members out of match
                this.interested.clear();
            } else if (!this.interested.containsValue(State.PENDING)) {
                // ask servers to dispatch the relevant players
                this.phase = Phase.DEPART;
                this.interested.entrySet().forEach(entry -> entry.setValue(State.PENDING));
                this.time_until_timeout = 10;
                this.server = this.handler.getPlugin().getBestServerFor(this.content, this.interested.size());
                for (Map.Entry<UUID, State> entry : this.interested.entrySet()) {
                    ProxiedPlayer player = this.handler.getPlugin().getProxy().getPlayer(entry.getKey());
                    if (player != null && player.isConnected()) {
                        if (player.getServer().getInfo() == this.server) {
                            entry.setValue(State.YES); // can skip actual departure since we are already on the server
                        } else {
                            ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_DEPART, player);
                            composed.writeUTF(this.id.toString());
                            this.handler.getPlugin().send(player.getServer(), composed.toByteArray());
                        }
                    } else {
                        entry.setValue(State.NO);
                    }
                }
            }
        } else if (getPhase() == Phase.DEPART) {
            if (!this.interested.containsValue(State.PENDING)) {
                // identify server with the least number of players
                this.phase = Phase.FINISH;
                if (this.server == null) {
                    // inform about matching failure (could not find server)
                    for (Map.Entry<UUID, State> entry : this.interested.entrySet()) {
                        ProxiedPlayer player = this.handler.getPlugin().getProxy().getPlayer(entry.getKey());
                        if (player != null && player.isConnected()) {
                            ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_FAIL, player);
                            composed.writeUTF(this.id.toString());
                            composed.writeUTF("match_fail_no_server");
                            this.handler.getPlugin().send(player.getServer(), composed.toByteArray());
                        }
                    }
                    // take members out of match
                    this.interested.clear();
                } else {
                    // transfer players to the target server
                    List<ProxiedPlayer> party = new ArrayList<>();
                    this.interested.forEach((member, response) -> {
                        if (response == State.NO) {
                            // kick if not ready to depart
                            ProxiedPlayer player = this.handler.getPlugin().getProxy().getPlayer(member);
                            if (player != null && player.isConnected()) {
                                ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_FAIL, player);
                                composed.writeUTF(this.id.toString());
                                composed.writeUTF("match_fail_cannot_depart");
                                this.handler.getPlugin().send(player.getServer(), composed.toByteArray());
                            }
                        } else if (response == State.YES) {
                            // move servers if ready to depart
                            ProxiedPlayer player = this.handler.getPlugin().getProxy().getPlayer(member);
                            if (player != null && player.isConnected()) {
                                party.add(player);
                                if (player.getServer().getInfo() != this.server) {
                                    player.connect(this.server, ServerConnectEvent.Reason.PLUGIN);
                                }
                            }
                        }
                    });
                    // force a group, queue server preparation
                    if (!party.isEmpty()) {
                        // force players to party up
                        BungeeParty existing_party = this.handler.getPlugin().getMatchHandler().getPartyById(this.party_id);
                        if (existing_party == null) {
                            this.handler.getPlugin().getMatchHandler().forceIntoParty(this.party_id, party);
                        }
                        // queue 'arrival' to prepare dungeon
                        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_FINISH);
                        composed.writeUTF(party_id);
                        composed.writeInt(party.size());
                        for (ProxiedPlayer player : party) {
                            composed.writeUTF(player.getName());
                        }
                        composed.writeUTF(content);
                        this.handler.getPlugin().send(this.server, composed.toByteArray(), true);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Internal state of a player state.
     */
    public enum State {
        PENDING,
        YES,
        NO,
    }

    /**
     * The respective phases to build a cross-server match.
     */
    public enum Phase {
        /**
         * Ask users if they want to participate in dungeon, fail if
         * anyone responds negatively.
         * <p>
         * Timeout of 30 seconds
         */
        ASKING,
        /**
         * Verify if user is ready to transfer, fail if anyone responds
         * negatively.
         * <p>
         * Timeout of 10 seconds
         */
        VERIFY,
        /**
         * Request players to depart their server and join the dungeon
         * instance.
         * <p>
         * Cut-off point, match cannot fail anymore. The match will simply
         * happen without the other players.
         * <p>
         * Timeout of 10 seconds
         */
        DEPART,
        /**
         * Not actually used, just there for completeness. This is set when
         * the last phase is finished.
         */
        FINISH
    }
}
