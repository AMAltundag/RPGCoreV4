package com.blutkrone.rpgproxy.group;

import com.blutkrone.rpgproxy.RPGProxy;
import com.blutkrone.rpgproxy.group.matching.IMatchPhase;
import com.blutkrone.rpgproxy.group.matching.MatchStateAsking;
import com.blutkrone.rpgproxy.group.matching.MatchStateTransfer;
import com.blutkrone.rpgproxy.group.matching.MatchStateVerify;
import com.blutkrone.rpgproxy.util.BungeeConfig;
import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

public class BungeeGroupHandler {

    // counter to keep track of unique party ID
    public static int PARTY_ID_COUNTER = 0;

    // plugin we are contained by
    private RPGProxy plugin;

    // players mapped to their match-maker phase
    private Map<UUID, IMatchPhase> matchmaker_active = new HashMap<>();
    // content mapped to players queued up
    private Map<String, Set<UUID>> matchmaker_interests = new HashMap<>();
    // parties registered to the proxy
    private Map<String, BungeeParty> parties = new HashMap<>();
    // cache tracking party by UUID
    private Map<UUID, BungeeParty> party_cache = new HashMap<>();

    public BungeeGroupHandler(RPGProxy plugin) {
        this.plugin = plugin;
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
     * @param receiver Who was the message sent to.
     * @param channel  What channel was the message sent to.
     * @param data     What data did it contain.
     * @return Whether it was parsed by this handler
     */
    public boolean read(ProxiedPlayer receiver, String channel, ByteArrayDataInput data) {
        if (BungeeTable.SERVER_PARTY_ADD.equals(channel)) {
            // silent add to party
            ProxiedPlayer adding = getPlugin().getProxy().getPlayer(data.readUTF());
            if (adding != null && getPartyFor(adding) == null && !isActivelyMatching(adding)) {
                BungeeParty party = getPartyById(data.readUTF());
                if (party != null && party.getMembersUUID().size() < 6) {
                    ProxiedPlayer leader = getPlugin().getProxy().getPlayer(data.readUTF());
                    if (party.isLeader(leader)) {
                        party.getMembersUUID().add(receiver.getUniqueId());
                        party.broadcast();
                        for (ProxiedPlayer party_player : party.getMembersOnline()) {
                            getPlugin().sendTranslatedMessage(party_player, "group_member_join", adding.getName());
                        }
                        this.party_cache.put(adding.getUniqueId(), party);
                        return true;
                    }
                }
            }

            getPlugin().sendTranslatedMessage(receiver, "group_internal_error");
        } else if (BungeeTable.SERVER_PARTY_ASK_LEADER.equals(channel)) {
            // deploy a party join request to the leader
            if (getPartyFor(receiver) == null) {
                ProxiedPlayer leader = getPlugin().getProxy().getPlayer(data.readUTF());
                if (leader != null) {
                    BungeeParty party = getPartyFor(leader);
                    if (party.isLeader(leader) && party.getMembersUUID().size() < 6) {
                        ByteArrayDataOutput answer = ByteStreams.newDataOutput();
                        answer.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                        answer.writeUTF(BungeeTable.PROXY_PARTY_ASK_LEADER);
                        answer.writeUTF(receiver.getName());
                        answer.writeUTF(party.getId());
                        getPlugin().sendData(leader, answer.toByteArray());
                        return true;
                    }
                }
            }

            getPlugin().sendTranslatedMessage(receiver, "group_internal_error");
        } else if (BungeeTable.SERVER_PARTY_ASK_STRANGER.equals(channel)) {
            // deploy a party invitation to a stranger
            ProxiedPlayer stranger = getPlugin().getProxy().getPlayer(data.readUTF());
            if (stranger != null && getPartyFor(stranger) == null) {
                BungeeParty party = getPartyFor(receiver);
                if (party == null) {
                    // create and deploy the party info
                    party = new BungeeParty(this, "auto_generated_" + PARTY_ID_COUNTER++);
                    party.setLeader(receiver.getUniqueId());
                    party.getMembersUUID().add(receiver.getUniqueId());
                    this.parties.put(party.getId(), party);
                    party.broadcast();
                    this.party_cache.put(receiver.getUniqueId(), party);
                    // deploy an invite
                    ByteArrayDataOutput answer = ByteStreams.newDataOutput();
                    answer.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                    answer.writeUTF(BungeeTable.PROXY_PARTY_ASK_STRANGER);
                    answer.writeUTF(receiver.getName());
                    answer.writeUTF(party.getId());
                    getPlugin().sendData(stranger, answer.toByteArray());
                    return true;
                } else if (party.isLeader(receiver) && party.getMembersUUID().size() < 6) {
                    ByteArrayDataOutput answer = ByteStreams.newDataOutput();
                    answer.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                    answer.writeUTF(BungeeTable.PROXY_PARTY_ASK_STRANGER);
                    answer.writeUTF(receiver.getName());
                    answer.writeUTF(party.getId());
                    getPlugin().sendData(stranger, answer.toByteArray());
                    return true;
                }
            }

            getPlugin().sendTranslatedMessage(receiver, "group_internal_error");
        } else if (BungeeTable.SERVER_PARTY_WANT_KICK.equals(channel)) {
            // kick a player out of a party
            BungeeParty party = getPartyFor(receiver);
            if (party.isLeader(receiver)) {
                String kicked_name = data.readUTF();
                UUID who = this.getPlugin().getLastKnownUUID(kicked_name);
                if (who != null && !party.isLeader(who)) {
                    // remove the offending player from the party
                    party.getMembersUUID().remove(who);
                    party.broadcast();
                    for (ProxiedPlayer party_player : party.getMembersOnline()) {
                        getPlugin().sendTranslatedMessage(party_player, "group_member_quit", kicked_name);
                    }
                    // inform everyone about the change
                    ProxiedPlayer maybe_online = getPlugin().getProxy().getPlayer(who);
                    if (maybe_online != null) {
                        getPlugin().sendTranslatedMessage(maybe_online, "group_you_were_kicked");
                    }
                    return true;
                }
            }

            getPlugin().sendTranslatedMessage(receiver, "group_internal_error");
        } else if (BungeeTable.SERVER_PARTY_WANT_QUIT.equals(channel)) {
            // want to leave party out of own accord
            BungeeParty party = getPartyFor(receiver);
            if (party != null) {
                // update the party that want to leave
                party.getMembersUUID().remove(receiver.getUniqueId());
                this.party_cache.remove(receiver.getUniqueId());
                if (party.getMembersUUID().isEmpty()) {
                    this.parties.remove(party.getId());
                } else {
                    party.setLeader(party.getMembersUUID().iterator().next());
                }
                party.broadcast();
                // inform everyone involved about the change
                getPlugin().sendTranslatedMessage(receiver, "group_you_have_quit");
                for (ProxiedPlayer party_player : party.getMembersOnline()) {
                    getPlugin().sendTranslatedMessage(party_player, "group_member_quit", receiver.getName());
                }
                return true;
            }

            getPlugin().sendTranslatedMessage(receiver, "group_internal_error");
        } else if (BungeeTable.SERVER_PARTY_CREATE.equals(channel)) {
            // identify (un-partied) players that can party up
            String id = data.readUTF();
            int size = data.readInt();
            List<UUID> members = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                UUID uuid = UUID.fromString(data.readUTF());
                if (getPartyFor(uuid) == null) {
                    members.add(uuid);
                }
            }
            if (members.isEmpty()) {
                return true;
            }
            // create and deploy the party info
            BungeeParty party = new BungeeParty(this, id);
            party.setLeader(members.iterator().next());
            party.getMembersUUID().addAll(members);
            this.parties.put(id, party);
            party.broadcast();
            // update party cache
            for (UUID member : members) {
                this.party_cache.put(member, party);
            }
            // inform everyone about the party establishment
            for (ProxiedPlayer member : party.getMembersOnline()) {
                for (ProxiedPlayer other : party.getMembersOnline()) {
                    getPlugin().sendTranslatedMessage(member, "group_member_join", other.getName());
                }
            }
        } else if (BungeeTable.SERVER_MATCH_UPDATE.equals(channel)) {
            // identify our new interests
            String[] arr = new String[data.readInt()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = data.readUTF();
            }


            BungeeParty party = getPartyFor(receiver);
            if (party != null) {
                // ensure no other matching process is active
                for (ProxiedPlayer player : party.getMembersOnline()) {
                    terminateMatching(player);
                }
                // ensure we are the leader
                if (party.isLeader(receiver) && arr.length != 0) {
                    MatchStateTransfer state = new MatchStateTransfer(this, party, arr[0]);
                    // deploy a request to group up
                    ByteArrayDataOutput answer = ByteStreams.newDataOutput();
                    answer.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                    answer.writeUTF(BungeeTable.PROXY_MATCH_TRANSFER_DEPART);
                    byte[] bytes = answer.toByteArray();
                    for (UUID uuid : party.getMembersUUID()) {
                        this.matchmaker_active.put(uuid, state);
                        getPlugin().sendData(uuid, bytes);
                    }
                } else {
                    getPlugin().sendTranslatedMessage(receiver, "group_internal_error");
                }
            } else {
                // stop any on-going matching effort
                terminateMatching(receiver);
                // take us out of existing queues
                this.matchmaker_interests.forEach((content, queued) -> queued.remove(receiver.getUniqueId()));
                // insert us into the new queues
                for (String content : arr) {
                    if (getPlugin().getConfig().matchmaker.containsKey(content)) {
                        this.matchmaker_interests.computeIfAbsent(content, (k -> new HashSet<>())).add(receiver.getUniqueId());
                    }
                }
                // if possible find a match
                findMatchFor(receiver.getUniqueId(), arr);
            }
        } else if (BungeeTable.SERVER_MATCH_ACCEPT.equals(channel)) {
            // player accepted matchmaker request
            UUID match_id = UUID.fromString(data.readUTF());
            handleMatchPhase(receiver, MatchStateAsking.class, match_id, true);
        } else if (BungeeTable.SERVER_MATCH_DECLINE.equals(channel)) {
            // player declined matchmaker request
            UUID match_id = UUID.fromString(data.readUTF());
            handleMatchPhase(receiver, MatchStateAsking.class, match_id, false);
            this.matchmaker_active.remove(receiver.getUniqueId());
        } else if (BungeeTable.SERVER_MATCH_VERIFY_PASSED.equals(channel)) {
            // server confirmed player is ready to play
            UUID match_id = UUID.fromString(data.readUTF());
            handleMatchPhase(receiver, MatchStateVerify.class, match_id, true);
        } else if (BungeeTable.SERVER_MATCH_VERIFY_FAILED.equals(channel)) {
            // server found an issue with the player
            UUID match_id = UUID.fromString(data.readUTF());
            handleMatchPhase(receiver, MatchStateVerify.class, match_id, false);
            this.matchmaker_active.remove(receiver.getUniqueId());
        } else if (BungeeTable.SERVER_MATCH_TRANSFER_SUCCESS.equals(channel)) {
            // server confirms player is ready to transfer away
            UUID match_id = UUID.fromString(data.readUTF());
            handleMatchPhase(receiver, MatchStateTransfer.class, match_id, true);
        } else if (BungeeTable.SERVER_MATCH_TRANSFER_FAILED.equals(channel)) {
            // server failed getting the player transfer ready
            UUID match_id = UUID.fromString(data.readUTF());
            handleMatchPhase(receiver, MatchStateTransfer.class, match_id, false);
            this.matchmaker_active.remove(receiver.getUniqueId());
        } else {
            return false;
        }

        return true;
    }

    /**
     * Should a group of players with common interests be found, they
     * are in an active matching process (that may fail).
     *
     * @param player Whose matches to check for.
     * @return Whether they have a match now.
     */
    public boolean isActivelyMatching(ProxiedPlayer player) {
        return this.matchmaker_active.containsKey(player.getUniqueId());
    }

    /**
     * Should a group of players with common interests be found, they
     * are in an active matching process (that may fail).
     *
     * @param player Whose matches to check for.
     * @return Whether they have a match now.
     */
    public boolean isActivelyMatching(UUID player) {
        return this.matchmaker_active.containsKey(player);
    }

    /**
     * Request a matchmaker termination for the given player, do
     * note that this will also kill the matchmaker process if it
     * started.
     *
     * @param player Who wants to terminate
     */
    public void terminateMatching(ProxiedPlayer player) {
        // update queued contents to nothing
        this.matchmaker_interests.forEach((content, queued) -> queued.remove(player.getUniqueId()));
        // give negative feedback to matchmaker
        IMatchPhase phase = this.matchmaker_active.remove(player.getUniqueId());
        if (phase != null) {
            IMatchPhase updated = phase.negative(player);
            if (updated != phase) {
                for (UUID uuid : phase.getInvolved()) {
                    this.matchmaker_active.put(uuid, updated);
                }
            }
        }
    }

    /**
     * Retrieve the party by a unique identifier.
     *
     * @param id Unique party identifier
     * @return Party that has the ID
     */
    public BungeeParty getPartyById(String id) {
        return this.parties.get(id);
    }

    /**
     * Retrieve the party a player belongs to, this may
     * be null if they do not have a party.
     *
     * @param player Whose party do we want
     * @return The party they belong to
     */
    public BungeeParty getPartyFor(UUID player) {
        return party_cache.get(player);
    }

    /**
     * Retrieve the party a player belongs to, this may
     * be null if they do not have a party.
     *
     * @param player Whose party do we want
     * @return The party they belong to
     */
    public BungeeParty getPartyFor(ProxiedPlayer player) {
        return this.getPartyFor(player.getUniqueId());
    }

    /**
     * Force the players to enter a party.
     *
     * @param players Who wants to party up
     */
    public void forceParty(String id, List<ProxiedPlayer> players) {
        // kick everyone off their party
        for (ProxiedPlayer player : players) {
            forceQuitParty(player);
        }
        // form a new party
        BungeeParty party = new BungeeParty(this, id);
        party.setLeader(players.iterator().next().getUniqueId());
        for (ProxiedPlayer player : players) {
            party.getMembersUUID().add(player.getUniqueId());
        }
        this.parties.put(id, party);
        party.broadcast();
        // update party cache
        for (ProxiedPlayer player : players) {
            this.party_cache.put(player.getUniqueId(), party);
        }
    }

    /**
     * Force a player to quit the party.
     *
     * @param player Who should be forced to quit
     */
    public void forceQuitParty(ProxiedPlayer player) {
        BungeeParty party = getPartyFor(player);
        if (party != null) {
            party.getMembersUUID().remove(player.getUniqueId());
            if (party.getMembersUUID().isEmpty()) {
                this.parties.remove(party.getId());
            } else {
                // elect another leader
                party.setLeader(party.getMembersUUID().iterator().next());
                // inform about leaving party
                getPlugin().sendTranslatedMessage(player, "group_you_have_quit");
                // inform everyone else on who left
                for (ProxiedPlayer party_player : party.getMembersOnline()) {
                    getPlugin().sendTranslatedMessage(party_player, "group_member_quit", player.getName());
                }
            }
            party.broadcast();
            this.party_cache.remove(player.getUniqueId());
        }
    }

    /**
     * A byte-array representing a total update.
     *
     * @return byte array containing every party.
     */
    public byte[] getDataForFullPartyUpdate() {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.PROXY_PARTY_UPDATE_ALL);
        data.writeInt(parties.size());
        for (BungeeParty party : this.parties.values()) {
            party.dump(data);
        }

        return data.toByteArray();
    }

    /*
     * Search for a potential group of players that we can match
     * up with.
     *
     * @param player Who wants to create a match.
     * @param contents What contents are we interested in.
     */
    private MatchStateAsking findMatchFor(UUID player, String... contents) {
        // see if we have a qualified match
        for (String content : contents) {
            // check if we have enough players
            Set<UUID> candidates = this.matchmaker_interests.getOrDefault(content, Collections.emptySet());
            // check if have enough participants now
            BungeeConfig.MatchMakerInfo config = getPlugin().getConfig().matchmaker.get(content);
            if (config == null || candidates.size() < config.maximum_players) {
                continue;
            }
            // create the matching structure
            MatchStateAsking match = new MatchStateAsking(this, content);
            match.getInvolved().add(player);
            // fill up with other interested players
            for (UUID candidate : candidates) {
                if (isActivelyMatching(candidate)) {
                    continue;
                }
                if (match.getInvolved().size() >= config.maximum_players) {
                    break;
                }
                match.getInvolved().add(candidate);
            }
            // keep track of the match
            for (UUID uuid : match.getInvolved()) {
                this.matchmaker_active.put(uuid, match);
            }
            // inform players about the potential match
            match.inform();
            // queue the match up that we found
            return match;
        }

        return null;
    }

    /*
     * Assistance function which can handle match phase transition.
     *
     * @param player Whose matchmaking process to handle
     * @param clazz What kind of phase do we expect
     * @param uuid What matchmaker ID do we process
     * @param feedback What input were we given
     */
    private void handleMatchPhase(ProxiedPlayer player, Class<? extends IMatchPhase> clazz, UUID uuid, boolean feedback) {
        // ensure we can update this phase
        IMatchPhase phase = this.matchmaker_active.get(player.getUniqueId());
        if (phase == null || phase.getClass() != clazz || !uuid.equals(phase.getUniqueID())) {
            getPlugin().sendTranslatedMessage(player, "group_internal_error");
            return;
        }
        // update the phase with the given feedback
        if (feedback) {
            phase = phase.positive(player);
        } else {
            phase = phase.negative(player);
        }
        // proliferate the phase change
        for (UUID involved : phase.getInvolved()) {
            this.matchmaker_active.put(involved, phase);
        }
    }
}