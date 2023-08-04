package com.blutkrone.rpgproxy.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * A utility meant to organize BungeeCord sub-channels for RPGCore.
 * <br>
 * The table used by the proxy should always be in sync with the
 * table used by the server.
 */
public final class BungeeTable {

    // general handling
    public static final String CHANNEL_BUNGEE = "BungeeCord";
    public static final String CHANNEL_RPGCORE = "blutkrone:rpgcore";
    // useful utilities
    public static final String SERVER_BOUND_BASIC_MESSAGE = "server_bound:basic_message";
    public static final String SERVER_BOUND_LIST_PLAYER = "server_bound:list_player";
    public static final String PROXY_BOUND_DELIVER_CHAT = "proxy_bound:deliver_chat";
    // match-maker with cross-server queue (server talking to proxy)
    public static final String PROXY_BOUND_MATCH_UPDATE = "proxy_bound:match_update";
    public static final String PROXY_BOUND_MATCH_ACCEPT = "proxy_bound:match_accept";
    public static final String PROXY_BOUND_MATCH_DECLINE = "proxy_bound:match_decline";
    public static final String PROXY_BOUND_MATCH_VERIFY_PASSED = "proxy_bound:match_verify_passed";
    public static final String PROXY_BOUND_MATCH_VERIFY_FAILED = "proxy_bound:match_verify_failed";
    public static final String PROXY_BOUND_MATCH_DEPART_SUCCESS = "proxy_bound:match_depart_success";
    public static final String PROXY_BOUND_MATCH_DEPART_FAILED = "proxy_bound:match_depart_failed";
    // match-maker with cross-server queue (proxy talking to server)
    public static final String SERVER_BOUND_MATCH_ASK = "server_bound:match_ask";
    public static final String SERVER_BOUND_MATCH_FAIL = "server_bound:match_fail";
    public static final String SERVER_BOUND_MATCH_VERIFY = "server_bound:match_verify";
    public static final String SERVER_BOUND_MATCH_DEPART = "server_bound:match_depart";
    public static final String SERVER_BOUND_MATCH_FINISH = "server_bound:match_finish";
    public static final String SERVER_BOUND_MATCH_INFO_ALL = "server_bound:match_info_all";
    // players grouping up as a party (proxy talking to server)
    public static final String SERVER_BOUND_PARTY_UPDATE_ONE = "server_bound:party_update_one_party";
    public static final String SERVER_BOUND_PARTY_UPDATE_ALL = "server_bound:party_update_all_party";
    public static final String SERVER_BOUND_PARTY_ASK_LEADER = "server_bound:party_ask_leader";
    public static final String SERVER_BOUND_PARTY_ASK_STRANGER = "server_bound:party_ask_stranger";
    // players grouping up as a party (server talking to proxy)
    public static final String PROXY_BOUND_PARTY_ASK_LEADER = "proxy_bound:party_ask_leader";
    public static final String PROXY_BOUND_PARTY_ASK_STRANGER = "proxy_bound:party_ask_stranger";
    public static final String PROXY_BOUND_PARTY_ADD = "proxy_bound:party_add";
    public static final String PROXY_BOUND_PARTY_REFUSE = "proxy_bound:party_refuse";
    public static final String PROXY_BOUND_PARTY_WANT_KICK = "proxy_bound:party_want_kick";
    public static final String PROXY_BOUND_PARTY_WANT_QUIT = "proxy_bound:party_want_quit";

    BungeeTable() {
    }

    /**
     * Prepare a byte array to be deployed to a server running
     * RPGCore with bungeecord handling.
     *
     * @return Prepared output
     */
    public static ByteArrayDataOutput compose(String topic) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(topic);
        data.writeUTF(new UUID(0, 0).toString());
        return data;
    }

    /**
     * Prepare a byte array to be deployed to a server running
     * RPGCore with bungeecord handling.
     *
     * @param topic   What topic is the message about
     * @param subject Who is related to the subject
     * @return Prepared output
     */
    public static ByteArrayDataOutput compose(String topic, ProxiedPlayer subject) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(topic);
        data.writeUTF(subject.getUniqueId().toString());
        return data;
    }
}
