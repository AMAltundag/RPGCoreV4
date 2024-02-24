package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HologramAddCommand extends AbstractCommand {
    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin") && sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return buildHelpText("§fcontent §8billboard ~x ~y ~z", "§cCreates a hologram [ADMIN]",
                "§7Content: What language identifier to show",
                "§8Billboard: Lock the axis",
                "§8X: Location of hologram (~ for relative)",
                "§8Y: Location of hologram (~ for relative)",
                "§8Z: Location of hologram (~ for relative)");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (args.length >= 2) {
            Location loc = ((Player) sender).getLocation().clone();
            String lc_text = args[1];
            boolean locked = false;

            if (args.length >= 3) {
                locked = args[2].equalsIgnoreCase("true");
            }
            if (args.length >= 4) {
                boolean relative = args[3].startsWith("~");
                double number = Double.valueOf(args[3].replace("~", ""));
                loc.setX((relative ? loc.getX() : 0d) + number);
            }
            if (args.length >= 5) {
                boolean relative = args[4].startsWith("~");
                double number = Double.valueOf(args[4].replace("~", ""));
                loc.setY((relative ? loc.getY() : 0d) + number);
            }
            if (args.length >= 6) {
                boolean relative = args[5].startsWith("~");
                double number = Double.valueOf(args[5].replace("~", ""));
                loc.setZ((relative ? loc.getZ() : 0d) + number);
            }

            // invert direction to look back at source
            loc.setDirection(loc.getDirection().multiply(-1));
            // create a hologram
            RPGCore.inst().getHologramManager().createHologram(loc, lc_text, locked);
            // inform about success
            String msg = RPGCore.inst().getLanguageManager().getTranslation("command_success");
            sender.sendMessage(msg);
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
