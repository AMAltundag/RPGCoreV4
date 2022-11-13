package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.mob.CoreCreature;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.List;

public class SpawnMobCommand extends AbstractCommand {
    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin") && sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("<mob> <*level> §fSpawn a mob where you are looking at.");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(((Player) sender));
        if (player == null) {
            return;
        }

        if (args.length >= 2) {
            if (RPGCore.inst().getMobManager().getIndex().has(args[1])) {
                // grab the template for the creature
                CoreCreature creature = RPGCore.inst().getMobManager().getIndex().get(args[1]);
                // identify level to spawn creature with
                int level = 1;
                if (args.length >= 3) {
                    try {
                        level = Integer.parseInt(args[2]);
                    } catch (Exception ignored) {
                        level = -1;
                    }
                }
                // spawn mob if we have a valid level
                if (level != -1) {
                    RayTraceResult where = ((Player) sender).rayTraceBlocks(24d);
                    if (where != null && where.getHitBlock() != null) {
                        // spawn at traced location
                        Location position = where.getHitBlock().getRelative(BlockFace.UP).getLocation();
                        creature.spawn(position, level);
                        // inform about success
                        String msg = RPGCore.inst().getLanguageManager().getTranslation("command_success");
                        sender.sendMessage(msg);
                        // prevents firing failure warning
                        return;
                    }
                }
            }
        }

        // spawn failed, give a warning
        String msg = RPGCore.inst().getLanguageManager().getTranslation("command_failed");
        sender.sendMessage(msg);
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        if (args.length == 2) {
            // offer all mob templates that match the prefix
            List<String> choices = new ArrayList<>();
            for (CoreCreature creature : RPGCore.inst().getMobManager().getIndex().getAll()) {
                choices.add(creature.getId());
            }
            choices.removeIf(choice -> !choice.startsWith(args[1]));
            return choices;
        }

        return null;
    }
}
