package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SocialCommand extends AbstractCommand {

    private Map<UUID, Long> cooldown = new HashMap<>();

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("<player> §fSocial menu for target player");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        // enforce internal 3s cooldown to not spam click
        long cooldown = this.cooldown.getOrDefault(((Player) sender).getUniqueId(), 0L);
        if (cooldown > System.currentTimeMillis()) {
            return;
        }
        this.cooldown.put(((Player) sender).getUniqueId(), System.currentTimeMillis() + 3000);
        // process the command
        if (args.length == 2) {
            try {
                UUID uuid = UUID.fromString(args[1]);
                RPGCore.inst().getHUDManager().getSocialMenu().present(((Player) sender), uuid);
            } catch (Exception ex) {
                UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                RPGCore.inst().getHUDManager().getSocialMenu().present(((Player) sender), uuid);
            }
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
