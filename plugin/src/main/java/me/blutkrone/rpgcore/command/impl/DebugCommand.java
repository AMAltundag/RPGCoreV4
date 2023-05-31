package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.menu.AbstractCoreMenu;
import me.blutkrone.rpgcore.util.fontmagic.FontMagicConstant;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

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
        new Test().finish(((Player) sender));
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

    private class Test extends AbstractCoreMenu {

        public Test() {
            super(3);
        }

        @Override
        public void rebuild() {
            MagicStringBuilder msb = new MagicStringBuilder();

            for (int i = 0; i < 50; i++) {
                String advance = FontMagicConstant.advance(i);
                Bukkit.getLogger().severe("DEPTH " + i + " WITH " + StringEscapeUtils.escapeJava(advance));
            }

            for (int i = 0; i < 50; i++) {
                if (i % 2 == 0) {
                    msb.advance(i).append(".", ChatColor.RED);
                } else {
                    msb.advance(i).append(".", ChatColor.BLUE);
                }
            }

            getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
        }

        @Override
        public boolean isTrivial() {
            return true;
        }
    }
}
