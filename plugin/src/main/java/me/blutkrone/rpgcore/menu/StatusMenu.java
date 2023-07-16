package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * A menu meant to organize attributes of a player in
 * a readable fashion.
 */
public class StatusMenu extends AbstractCoreMenu {

    private final Map<Integer, me.blutkrone.rpgcore.hud.menu.StatusMenu.StatusLayout> layout;

    public StatusMenu(Map<Integer, me.blutkrone.rpgcore.hud.menu.StatusMenu.StatusLayout> layout) {
        super(6);
        this.layout = layout;
    }

    @Override
    public void rebuild() {
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(this.getMenu().getViewer());

        // populate with status icons
        this.getMenu().clearItems();
        for (Map.Entry<Integer, me.blutkrone.rpgcore.hud.menu.StatusMenu.StatusLayout> entry : layout.entrySet()) {
            ItemStack item = ItemBuilder.of(Material.IRON_AXE).build();
            IChestMenu.setBrand(item, RPGCore.inst(), "style", entry.getValue().style);
            IChestMenu.setBrand(item, RPGCore.inst(), "icon", entry.getValue().icon);
            IChestMenu.setBrand(item, RPGCore.inst(), "attributes", String.join(",", entry.getValue().attributes));
            try {
                RPGCore.inst().getItemManager().getDescribers().get("status").describe(item, player);
            } catch (Exception e) {
                e.printStackTrace();
            }
            getMenu().setItemAt(entry.getKey(), item);
        }

        // build the basic background
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_player_status"), ChatColor.WHITE);
        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}