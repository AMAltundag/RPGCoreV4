package com.blutkrone.rpgproxy.util;

/**
 * A utility meant to organize BungeeCord sub-channels for RPGCore.
 * <p>
 * The table used by the proxy should always be in sync with the
 * table used by the server.
 */
public final class BungeeTable {

    // base channel for RPGCore
    public static final String CHANNEL_RPGCORE = "blutkrone:rpgcore";
    // base channel for BungeeCord
    public static final String CHANNEL_BUNGEE = "BungeeCord";

    // proxy wants to send a translated message
    public static final String PROXY_BASIC_MESSAGE = "proxy_basic_message";

    // update match seeking status
    public static final String SERVER_MATCH_UPDATE = "server_match_decline";
    // accept a match offer
    public static final String SERVER_MATCH_ACCEPT = "server_match_decline";
    // reject a match offer
    public static final String SERVER_MATCH_DECLINE = "server_match_decline";
    // everything is fine, we can do the match
    public static final String SERVER_MATCH_VERIFY_PASSED = "server_match_verify_passed";
    // something went wrong, cancel the match
    public static final String SERVER_MATCH_VERIFY_FAILED = "server_match_verify_failed";
    // something went wrong, cancel the match
    public static final String SERVER_MATCH_TRANSFER_SUCCESS = "server_match_transfer_success";
    // something went wrong, cancel the match
    public static final String SERVER_MATCH_TRANSFER_FAILED = "server_match_transfer_failed";

    // proxy found a match and asks about it
    public static final String PROXY_MATCH_ASK = "proxy_match_ask";
    // someone rejected to play the match
    public static final String PROXY_MATCH_REJECT = "proxy_match_reject";
    // everyone accepted, get ready to transfer
    public static final String PROXY_MATCH_VERIFY = "proxy_match_verify";
    // get ready to depart from this server
    public static final String PROXY_MATCH_TRANSFER_DEPART = "proxy_match_transfer_depart";
    // party is slowly arriving on server
    public static final String PROXY_MATCH_TRANSFER_ARRIVE = "proxy_match_transfer_arrive";
    // unexpected issue when trying to depart
    public static final String PROXY_MATCH_TRANSFER_ERROR = "proxy_match_transfer_error";

    // update on info of a specific party
    public static final String PROXY_PARTY_UPDATE_ONE = "proxy_party_update_one_party";
    // total data update of every party
    public static final String PROXY_PARTY_UPDATE_ALL = "proxy_party_update_all_party";
    // ask a party leader if stranger can be added
    public static final String PROXY_PARTY_ASK_LEADER = "proxy_party_ask_leader";
    // ask a stranger if they want to join leaders party
    public static final String PROXY_PARTY_ASK_STRANGER = "proxy_party_ask_stranger";

    // ask a party leader if stranger can be added
    public static final String SERVER_PARTY_ASK_LEADER = "server_party_ask_leader";
    // ask a stranger if they want to join leaders party
    public static final String SERVER_PARTY_ASK_STRANGER = "server_party_ask_stranger";
    // add someone to a party
    public static final String SERVER_PARTY_ADD = "server_party_add";
    // a (possible) leader wants to kick a player
    public static final String SERVER_PARTY_WANT_KICK = "server_party_want_kick";
    // someone wants a new party to be created
    public static final String SERVER_PARTY_WANT_QUIT = "server_party_want_quit";
    // create a new party
    public static final String SERVER_PARTY_CREATE = "server_party_create";

    BungeeTable() {
    }
}
