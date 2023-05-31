package me.blutkrone.rpgcore.social.server;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.api.social.IPlayerHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerPlayerHandler implements IPlayerHandler {

    private List<String> players = new ArrayList<>();

    public ServerPlayerHandler() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RPGCore.inst(), () -> {
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

    @Override
    public void talk(Player player, String channel, BaseComponent[] message) {
        if (channel.startsWith("@")) {
            Player target = Bukkit.getPlayer(channel.substring(1));
            if (target != null) {
                RPGCore.inst().getVolatileManager().sendMessage(message, Arrays.asList(player, target));
            } else {
                RPGCore.inst().getLanguageManager().sendMessage(player, "chat_whisper_not_found", channel.substring(1));
            }
        } else if (channel.equals("party")) {
            IPartySnapshot party = RPGCore.inst().getSocialManager().getGroupHandler().getPartySnapshot(player);
            if (party != null) {
                List<Player> online = new ArrayList<>();
                for (OfflinePlayer offline : party.getAllMembers()) {
                    Player online_player = offline.getPlayer();
                    if (online_player != null) {
                        online.add(online_player);
                    }
                }
                RPGCore.inst().getVolatileManager().sendMessage(message, online);
            } else {
                RPGCore.inst().getLanguageManager().sendMessage(player, "chat_you_have_no_party");
            }
        } else {
            List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
            RPGCore.inst().getVolatileManager().sendMessage(message, online);
        }
    }
}
