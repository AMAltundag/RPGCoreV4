package me.blutkrone.rpgcore.social.server;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.social.IPlayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerPlayerHandler implements IPlayerHandler {

    private List<String> players = new ArrayList<>();

    public ServerPlayerHandler() {
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            this.players = Collections.unmodifiableList(players);
        }, 1, 100);
    }

    @Override
    public List<String> getConnectedPlayers() {
        return this.players;
    }
}
