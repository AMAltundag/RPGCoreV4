package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.command.AbstractCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DebugCommand extends AbstractCommand {
    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public BaseComponent[] getHelpText() {
        return new BaseComponent[0];
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        // NavigationMenu.Cartography cartography = new NavigationMenu.Cartography(((Player) sender).getLocation());
        // CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(((Player) sender));
        // if (core_player != null) {
        //     // grab the markers on the map
        //     List<MapMarker> markers = RPGCore.inst().getMinimapManager()
        //             .getMarkersOf(((Player) sender), core_player);
        //     // track the markers on the map
        //     for (MapMarker marker : markers) {
        //         if (marker.getLocation().distance(((Player) sender).getLocation()) <= marker.distance) {
        //             cartography.addMarker(marker);
        //         }
        //     }
        // }
        // cartography.finish(((Player) sender));
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }
}
