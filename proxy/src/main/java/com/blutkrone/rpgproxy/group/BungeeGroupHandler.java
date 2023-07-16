package com.blutkrone.rpgproxy.group;

import com.blutkrone.rpgproxy.RPGProxy;
import com.blutkrone.rpgproxy.util.BungeeConfig;
import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class BungeeGroupHandler implements Listener {

    public static final Object SYNC = new Object();

    // counter to keep track of unique party ID
    public static int PARTY_ID_COUNTER = 0;

    // plugin we are contained by
    private RPGProxy plugin;
    // players mapped to their match-maker phase
    private Queue<Matching> matchmaker = new ConcurrentLinkedQueue<>();
    // content mapped to players queued up
    private Map<String, Set<UUID>> matchmaker_interests = new ConcurrentHashMap<>();
    // parties registered to the proxy
    private Map<String, BungeeParty> parties = new ConcurrentHashMap<>();
    // cache tracking party by UUID
    private Map<UUID, BungeeParty> party_cache = new ConcurrentHashMap<>();
    // flag matching process as dirty
    private boolean matching_dirty = false;

    public BungeeGroupHandler(RPGProxy plugin) {
        this.plugin = plugin;
        this.plugin.getProxy().getPluginManager().registerListener(plugin, this);
        this.plugin.getProxy().getScheduler().schedule(this.plugin, () -> {
            // tick every matchmaker to get rid of them
            synchronized (SYNC) {
                this.matchmaker.removeIf(Matching::tick);
            }
            // check for lonely parties
            List<BungeeParty> lonely = new ArrayList<>();
            for (BungeeParty party : this.parties.values()) {
                if (party.getLonelyCounter() > 15) {
                    lonely.add(party);
                }
            }
            // second pass to verify integrity
            for (BungeeParty party : lonely) {
                synchronized (SYNC) {
                    if (party.getLonelyCounter() > 15) {
                        disbandParty(party);
                    }
                }
            }
        }, 1L, 1L, TimeUnit.SECONDS);
        this.plugin.getProxy().getScheduler().schedule(this.plugin, () -> {
            // update matchmaker views if necessary
            if (this.matching_dirty) {
                this.getPlugin().send(this.buildFullMatchInfo());
            }
        }, 1L, 5L, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onQuitServer(PlayerDisconnectEvent event) {
        // quit queue when disconnecting
        this.terminateMatching(event.getPlayer());
        // leave the party when disconnecting
        this.leaveParty(event.getPlayer().getUniqueId());
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
        if (BungeeTable.PROXY_BOUND_PARTY_ADD.equals(channel)) {
            BungeeParty party = getPartyById(data.readUTF());
            ProxiedPlayer target = getPlugin().getProxy().getPlayer(UUID.fromString(data.readUTF()));

            if (target == null || party == null) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "NO_TARGET_OR_PARTY");
            } else if (getPartyFor(target) != null) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "TARGET_HAS_GROUP");
            } else if (isActivelyMatching(target)) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "TARGET_HAS_MATCHMAKER");
            } else {
                // add player to the party
                synchronized (SYNC) {
                    if (party.size() < 6) {
                        party.addPlayer(target);
                        this.party_cache.put(target.getUniqueId(), party);
                    }
                }
                // broadcast the changes
                party.broadcast();
                for (ProxiedPlayer party_player : party.getMembersOnline()) {
                    getPlugin().sendTranslatedMessage(party_player, "group_member_join", target.getName());
                }
            }
        } else if (BungeeTable.PROXY_BOUND_PARTY_REFUSE.equals(channel)) {
            BungeeParty party = getPartyById(data.readUTF());
            ProxiedPlayer target = getPlugin().getProxy().getPlayer(UUID.fromString(data.readUTF()));
            // inform other party about rejection
            if (target != null) {
                getPlugin().sendTranslatedMessage(target, "group_rejected", whoAsked.getDisplayName());
            }
            // if rejected, and alone, disband the party
            if (party != null) {
                disbandIfLonely(party);
            }
        } else if (BungeeTable.PROXY_BOUND_PARTY_ASK_LEADER.equals(channel)) {
            ProxiedPlayer player_with_party = getPlugin().getProxy().getPlayer(data.readUTF());

            if (player_with_party == null) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "PLAYER_NOT_FOUND");
            } else if (getPartyFor(whoAsked) != null) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "PARTY_NOT_FOUND");
            } else {
                // check if we can ask the leader of the party
                String party_id = null;
                ProxiedPlayer leader = null;
                synchronized (SYNC) {
                    BungeeParty party = getPartyFor(player_with_party);
                    if (party != null && party.size() < 6) {
                        party_id = party.getId();
                        leader = party.getLeader();
                    }
                }
                // deploy the inquiry
                if (party_id == null || leader == null) {
                    getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "LEADER_NOT_FOUND");
                } else {
                    ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_PARTY_ASK_LEADER, leader);
                    composed.writeUTF(whoAsked.getUniqueId().toString());
                    composed.writeUTF(party_id);
                    getPlugin().send(leader.getServer(), composed.toByteArray());
                }
            }
        } else if (BungeeTable.PROXY_BOUND_PARTY_ASK_STRANGER.equals(channel)) {
            ProxiedPlayer stranger = getPlugin().getProxy().getPlayer(data.readUTF());

            // deploy a party invitation to a stranger
            if (stranger == null) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "PLAYER_NOT_FOUND");
            } else if (getPartyFor(stranger) != null) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "PLAYER_ALREADY_GROUPED");
            } else {
                // check if there is an existing party to take advantage of
                BungeeParty party = null;
                boolean broadcast = false;
                boolean allowed = false;
                synchronized (SYNC) {
                    party = getPartyFor(whoAsked);
                    if (party == null) {
                        party = new BungeeParty(this, "auto_generated_" + PARTY_ID_COUNTER++, whoAsked);
                        this.parties.put(party.getId(), party);
                        this.party_cache.put(whoAsked.getUniqueId(), party);
                        broadcast = true;
                    }
                    allowed = party.isLeader(whoAsked) && party.size() < 6;
                }
                // deploy a broadcast if party is new
                if (broadcast) {
                    party.broadcast();
                }
                // deploy an invitation if allowed
                if (!allowed) {
                    getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "UNAUTHORIZED_OR_FULL");
                } else {
                    ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_PARTY_ASK_STRANGER, stranger);
                    composed.writeUTF(whoAsked.getUniqueId().toString());
                    composed.writeUTF(party.getId());
                    getPlugin().send(stranger.getServer(), composed.toByteArray());
                }
            }
        } else if (BungeeTable.PROXY_BOUND_PARTY_WANT_KICK.equals(channel)) {
            String kicked_name = data.readUTF();
            UUID who = this.getPlugin().getLastKnownUUID(kicked_name);

            // kick a player out of a party
            BungeeParty party = getPartyFor(whoAsked);
            if (who == null || party == null) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "PARTY_OR_PLAYER_NOT_FOUND");
            } else if (!party.isLeader(whoAsked)) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "UNAUTHORIZED");
            } else if (who.equals(whoAsked.getUniqueId())) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "CANNOT_KICK_SELF");
            } else {
                // remove the offending player from the party
                synchronized (SYNC) {
                    leaveParty(who);
                }
                // inform everyone involved about the change
                party.broadcast();
                for (ProxiedPlayer party_player : party.getMembersOnline()) {
                    getPlugin().sendTranslatedMessage(party_player, "group_member_quit", kicked_name);
                }
                ProxiedPlayer maybe_online = getPlugin().getProxy().getPlayer(who);
                if (maybe_online != null) {
                    getPlugin().sendTranslatedMessage(maybe_online, "group_you_were_kicked");
                }
            }
        } else if (BungeeTable.PROXY_BOUND_PARTY_WANT_QUIT.equals(channel)) {
            // want to leave party out of own accord
            BungeeParty party = getPartyFor(whoAsked);
            if (party == null) {
                getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "PARTY_NOT_FOUND");
            } else {
                // update the party that want to leave
                synchronized (SYNC) {
                    leaveParty(whoAsked.getUniqueId());
                }
                // inform everyone involved about the change
                party.broadcast();
                getPlugin().sendTranslatedMessage(whoAsked, "group_you_have_quit");
                for (ProxiedPlayer party_player : party.getMembersOnline()) {
                    getPlugin().sendTranslatedMessage(party_player, "group_member_quit", whoAsked.getName());
                }
            }
        } else if (BungeeTable.PROXY_BOUND_MATCH_UPDATE.equals(channel)) {
            // identify our new interests
            String[] arr = new String[data.readInt()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = data.readUTF();
            }
            // process matchmaker handling
            synchronized (SYNC) {
                BungeeParty party = getPartyFor(whoAsked);
                if (party == null) {
                    // stop any on-going matching effort
                    terminateMatching(whoAsked);
                    // take us out of existing queues
                    this.matchmaker_interests.forEach((content, queued) -> queued.remove(whoAsked.getUniqueId()));
                    // insert us into the new queues
                    for (String content : arr) {
                        if (getPlugin().getConfig().matchmaker.containsKey(content)) {
                            this.matchmaker_interests.computeIfAbsent(content, (k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))).add(whoAsked.getUniqueId());
                        }
                    }
                    // if possible find a match
                    findMatchFor(whoAsked, arr);
                    // queue up for a view update
                    this.matching_dirty = true;
                } else if (party.isLeader(whoAsked)) {
                    // ensure no other matching process is active
                    for (ProxiedPlayer player : party.getMembersOnline()) {
                        terminateMatching(player);
                    }
                    // ensure we are the leader
                    if (arr.length != 0) {
                        this.matchmaker.add(new Matching(this, arr[0], party));
                    }
                    // queue up for a view update
                    this.matching_dirty = true;
                } else {
                    getPlugin().sendTranslatedMessage(whoAsked, "unexpected_error", "UNAUTHORIZED");
                }
            }
        } else if (BungeeTable.PROXY_BOUND_MATCH_ACCEPT.equals(channel)) {
            // player accepted matchmaker request
            synchronized (SYNC) {
                Matching matching = getMatching(whoAsked, Matching.Phase.ASKING);
                if (matching != null) {
                    matching.response(whoAsked.getUniqueId(), true);
                }
            }
        } else if (BungeeTable.PROXY_BOUND_MATCH_DECLINE.equals(channel)) {
            // player declined matchmaker request
            synchronized (SYNC) {
                Matching matching = getMatching(whoAsked, Matching.Phase.ASKING);
                if (matching != null) {
                    matching.response(whoAsked.getUniqueId(), false);
                }
            }
        } else if (BungeeTable.PROXY_BOUND_MATCH_VERIFY_PASSED.equals(channel)) {
            // server confirmed player is ready to play
            synchronized (SYNC) {
                Matching matching = getMatching(whoAsked, Matching.Phase.VERIFY);
                if (matching != null) {
                    matching.response(whoAsked.getUniqueId(), true);
                }
            }
        } else if (BungeeTable.PROXY_BOUND_MATCH_VERIFY_FAILED.equals(channel)) {
            // server found an issue with the player
            synchronized (SYNC) {
                Matching matching = getMatching(whoAsked, Matching.Phase.VERIFY);
                if (matching != null) {
                    matching.response(whoAsked.getUniqueId(), false);
                }
            }
        } else if (BungeeTable.PROXY_BOUND_MATCH_DEPART_SUCCESS.equals(channel)) {
            // server confirms player is ready to transfer away
            synchronized (SYNC) {
                Matching matching = getMatching(whoAsked, Matching.Phase.DEPART);
                if (matching != null) {
                    matching.response(whoAsked.getUniqueId(), true);
                }
            }
        } else if (BungeeTable.PROXY_BOUND_MATCH_DEPART_FAILED.equals(channel)) {
            // server failed getting the player transfer ready
            synchronized (SYNC) {
                Matching matching = getMatching(whoAsked, Matching.Phase.DEPART);
                if (matching != null) {
                    matching.response(whoAsked.getUniqueId(), false);
                }
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Request a matchmaker termination for the given player, do
     * note that this will also kill the matchmaker process if it
     * started.
     *
     * @param player Who wants to terminate
     */
    public void terminateMatching(ProxiedPlayer player) {
        synchronized (SYNC) {
            // update queued contents to nothing
            this.matchmaker_interests.forEach((content, queued) -> queued.remove(player.getUniqueId()));
            // give a negative response to matchmaker
            for (Matching matching : this.matchmaker) {
                if (matching.contains(player)) {
                    matching.response(player.getUniqueId(), false);
                }
            }
            // flag for needing matching update
            this.matching_dirty = true;
        }
    }

    /**
     * Retrieve the party a player belongs to, this may
     * be null if they do not have a party.
     *
     * @param player Whose party do we want
     * @return The party they belong to
     */
    public List<ProxiedPlayer> getPartyMembers(ProxiedPlayer player) {
        List<ProxiedPlayer> party_members = new ArrayList<>();
        synchronized (SYNC) {
            BungeeParty party = this.getPartyFor(player.getUniqueId());
            if (party != null) {
                party_members.addAll(party.getMembersOnline());
            }
        }
        return party_members;
    }

    /**
     * A byte-array representing a rebuild of the party view
     * for the server instances.
     *
     * @return byte array containing every party.
     */
    public byte[] buildFullPartyInfo() {
        // snapshot to ensure size-to-data ratio
        List<BungeeParty> parties = new ArrayList<>(this.parties.values());
        // compile into data that we can deploy
        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_PARTY_UPDATE_ALL);
        composed.writeInt(parties.size());
        for (BungeeParty party : parties) {
            synchronized (SYNC) {
                party.dump(composed);
            }
        }
        return composed.toByteArray();
    }

    /**
     * A byte-array representing a rebuild of the matchmaker view
     * for the server instances.
     *
     * @return byte array containing every matchmaker.
     */
    public byte[] buildFullMatchInfo() {
        // collect by interest
        Set<UUID> matching = new HashSet<>();
        for (Map.Entry<String, Set<UUID>> entry : this.matchmaker_interests.entrySet()) {
            matching.addAll(entry.getValue());
        }
        // collect by matching (iE full parties skip the 'interests' phase)
        for (Matching match : this.matchmaker) {
            matching.addAll(match.getInterested().keySet());
        }
        // compile into data that we can deploy
        ByteArrayDataOutput composed = BungeeTable.compose(BungeeTable.SERVER_BOUND_MATCH_INFO_ALL);
        composed.writeInt(matching.size());
        for (UUID uuid : matching) {
            composed.writeUTF(uuid.toString());
        }
        return composed.toByteArray();
    }

    /*
     * Disband the party if the party only has one player left
     * in it.
     * <p>
     * This will NOT broadcast the changes performed.
     *
     * @param party The party to disband
     */
    void disbandIfLonely(BungeeParty party) {
        boolean broadcast = false;
        // attempt to disband the party
        synchronized (SYNC) {
            if (party.getMembersUUID().size() <= 1) {
                this.parties.remove(party.getId());
                for (UUID uuid : party.getMembersUUID()) {
                    this.party_cache.remove(uuid);
                }

                for (ProxiedPlayer party_player : party.getMembersOnline()) {
                    getPlugin().sendTranslatedMessage(party_player, "group_you_have_quit");
                }

                party.getMembersUUID().clear();
                broadcast = true;
            }
        }
        // inform about disbanding party
        if (broadcast) {
            party.broadcast();
        }
    }

    /*
     * Disbands a party, this happens automatically should there only
     * be one player left in the party.
     * <p>
     * This will NOT broadcast the changes performed.
     *
     * @param party The party to disband
     */
    void disbandParty(BungeeParty party) {
        // notify players about leaving their party
        for (ProxiedPlayer party_player : party.getMembersOnline()) {
            getPlugin().sendTranslatedMessage(party_player, "group_you_have_quit");
        }
        // disband the party
        synchronized (SYNC) {
            this.parties.remove(party.getId());
            for (UUID uuid : party.getMembersUUID()) {
                this.party_cache.remove(uuid);
            }
            party.getMembersUUID().clear();
        }
        // broadcast the changes performed
        party.broadcast();
    }

    /*
     * Force a player to quit the party.
     *
     * @param player Who should be forced to quit
     */
    void leaveParty(UUID player) {
        BungeeParty party = getPartyFor(player);
        if (party != null) {
            // inform party members about the leaving player
            for (ProxiedPlayer party_player : party.getMembersOnline()) {
                if (party_player.getUniqueId().equals(player)) {
                    getPlugin().sendTranslatedMessage(party_player, "group_you_have_quit");
                } else {
                    getPlugin().sendTranslatedMessage(party_player, "group_member_quit", getPlugin().getLastKnownName(player));
                }
            }
            // quit the party, dissolve if now empty
            synchronized (SYNC) {
                // quit the party we are in
                party.removePlayer(player);
                this.party_cache.remove(player);
                // disband with only 1 player left
                if (party.getMembersUUID().size() <= 1) {
                    for (ProxiedPlayer party_player : party.getMembersOnline()) {
                        getPlugin().sendTranslatedMessage(party_player, "group_you_have_quit");
                    }
                    this.parties.remove(party.getId());
                    for (UUID uuid : party.getMembersUUID()) {
                        this.party_cache.remove(uuid);
                    }
                    party.getMembersUUID().clear();
                }
            }
            // broadcast the changes in the party
            party.broadcast();
        }
    }

    /*
     * Retrieve the current on-going matching effort of the given player, do note
     * that once a player responded negatively they are no longer considered to be
     * involved in the effort.
     *
     * @param player Whose matching effort to check
     * @param phases What phase are we allowed
     * @return The match we found
     */
    Matching getMatching(ProxiedPlayer player, Matching.Phase phase) {
        for (Matching matching : this.matchmaker) {
            if (matching.contains(player) && phase == matching.getPhase()) {
                return matching;
            }
        }

        return null;
    }

    /*
     * Should a group of players with common interests be found, they
     * are in an active matching process (that may fail).
     *
     * @param player Whose matches to check for.
     * @return Whether they have a match now.
     */
    boolean isActivelyMatching(ProxiedPlayer player) {
        for (Matching matching : this.matchmaker) {
            if (matching.contains(player)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Retrieve the party by a unique identifier.
     *
     * @param id Unique party identifier
     * @return Party that has the ID
     */
    BungeeParty getPartyById(String id) {
        return this.parties.get(id);
    }

    /*
     * Retrieve the party a player belongs to, this may
     * be null if they do not have a party.
     *
     * @param player Whose party do we want
     * @return The party they belong to
     */
    BungeeParty getPartyFor(UUID player) {
        return party_cache.get(player);
    }

    /*
     * Retrieve the party a player belongs to, this may
     * be null if they do not have a party.
     *
     * @param player Whose party do we want
     * @return The party they belong to
     */
    BungeeParty getPartyFor(ProxiedPlayer player) {
        return player == null ? null : this.getPartyFor(player.getUniqueId());
    }

    /*
     * Force the players to enter a party.
     *
     * @param players Who wants to party up
     */
    void forceIntoParty(String id, List<ProxiedPlayer> players) {
        // kick everyone off their party
        for (ProxiedPlayer player : players) {
            this.leaveParty(player.getUniqueId());
        }
        // form a new party
        BungeeParty party = new BungeeParty(this, id, players);
        this.parties.put(id, party);
        party.broadcast();
        // update party cache
        for (ProxiedPlayer player : players) {
            this.party_cache.put(player.getUniqueId(), party);
        }
    }

    /*
     * Search for a potential group of players that we can match
     * up with.
     *
     * @param player Who wants to create a match.
     * @param contents What contents are we interested in.
     */
    void findMatchFor(ProxiedPlayer player, String... contents) {
        // see if we have a qualified match
        for (String content : contents) {
            // check if we have enough players
            Set<UUID> candidates = this.matchmaker_interests.getOrDefault(content, Collections.emptySet());
            // check if have enough participants now
            BungeeConfig.MatchMakerInfo config = getPlugin().getConfig().matchmaker.get(content);
            if (config == null || candidates.size() < config.maximum_players) {
                continue;
            }
            // collect players we can utilize
            List<ProxiedPlayer> interested = new ArrayList<>();
            interested.add(player);
            for (UUID candidate : candidates) {
                ProxiedPlayer other = this.getPlugin().getProxy().getPlayer(candidate);
                if (other != null && interested.size() < config.maximum_players && !isActivelyMatching(other)) {
                    interested.add(other);
                }
            }
            // deploy a match request if we find anything
            if (interested.size() == config.maximum_players) {
                // queue the matchmaker up
                this.matchmaker.add(new Matching(this, content, interested));
                // queue the match up that we found
                return;
            }
        }
    }
}