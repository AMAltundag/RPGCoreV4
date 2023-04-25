package com.blutkrone.rpgproxy.util;

import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration of the RPGCore proxy plugin
 */
public class BungeeConfig {

    public Map<String, MatchMakerInfo> matchmaker = new HashMap<>();

    public BungeeConfig(Configuration config) {
        Configuration section = config.getSection("content-server");
        for (String key : section.getKeys()) {
            this.matchmaker.put(key, new MatchMakerInfo(section.getSection(key)));
        }
    }

    public BungeeConfig() {

    }

    public class MatchMakerInfo {
        public final int players;
        public final Set<String> servers;
        public final int maximum_players;

        public MatchMakerInfo(Configuration config) {
            this.players = config.getInt("players");
            this.maximum_players = config.getInt("maximum-players");
            this.servers = new HashSet<>(config.getStringList("servers"));
        }
    }
}
