package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.data.IDataIdentity;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Map;

public class RosterMenu extends AbstractCoreMenu {

    private int current_page;
    private me.blutkrone.rpgcore.hud.menu.RosterMenu.Page[] roster_pages;

    public RosterMenu(me.blutkrone.rpgcore.hud.menu.RosterMenu origin) {
        super(6);
        this.roster_pages = origin.roster_pages;
    }

    @Override
    public void rebuild() {
        // clear out all items on the menu
        getMenu().clearItems();

        // updated msb title for the menu
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_roster"), ChatColor.WHITE);

        // create icons to swap between pages
        for (int i = 0; i < 9; i++) {
            me.blutkrone.rpgcore.hud.menu.RosterMenu.Page page_icon = roster_pages[i];

            if (getMenu().getViewer().hasPermission(page_icon.permission)) {
                getMenu().setItemAt(45 + i, page_icon.icon_available);
            } else {
                getMenu().setItemAt(45 + i, page_icon.icon_locked);
            }
        }
        // create portraits of the current characters
        for (int i = 0; i < 3; i++) {
            String portrait = "nothing";

            // prepare default information
            ItemStack itemized = RPGCore.inst().getLanguageManager()
                    .getAsItem("empty_roster_slot", (current_page * 3) + i)
                    .build();

            // attempt to load character information, if we got any
            try {
                Map<String, DataBundle> raw_data = RPGCore.inst().getDataManager().getRawData(IDataIdentity.of(getMenu().getViewer().getUniqueId(), current_page * 3 + i));

                if (!raw_data.isEmpty()) {
                    String alias = raw_data.get("display").getString(1);
                    portrait = raw_data.get("display").getString(2);

                    itemized = RPGCore.inst().getLanguageManager().getAsItem("invisible")
                            .name("§fSlot #" + current_page * 3 + i)
                            .appendLore("§fAlias: '" + alias + "'")
                            .appendLore("§fPortrait: '" + portrait + "'")
                            .build();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            IChestMenu.setBrand(itemized, RPGCore.inst(), "roster-slot", String.valueOf(current_page * 3 + i));

            // set the tooltip items
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 4; k++) {
                    getMenu().setItemAt(k * 9 + (3 * i) + j, itemized);
                }
            }

            // contribute to the title
            msb.shiftToExact(54 * i).append(resourcepack().texture("static_" + portrait + "_portrait"), ChatColor.WHITE);
        }

        // InstructionBuilder instructions = new InstructionBuilder();
        // instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_roster"));
        // instructions.apply(msb);

        // supply the title to the player
        getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!isRelevant(event.getCurrentItem())) {
            return;
        }

        // check if we are doing a page swap
        for (int i = 0; i < roster_pages.length; i++) {
            me.blutkrone.rpgcore.hud.menu.RosterMenu.Page page = roster_pages[i];
            if (page.icon_available.isSimilar(event.getCurrentItem())) {
                this.current_page = i;
                getMenu().queryRebuild();
                return;
            }
        }

        // check if we are trying to log-on to a certain character
        int slot = Integer.parseInt(IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "roster-slot", "-1"));
        if (slot < 0) return;

        // joining should create the character for us
        try {
            CorePlayer core_player = RPGCore.inst().getDataManager().loadPlayer(getMenu().getViewer(), IDataIdentity.of(getMenu().getViewer().getUniqueId(), slot));
            RPGCore.inst().getEntityManager().register(getMenu().getViewer().getUniqueId(), core_player);
            getMenu().stalled(() -> getMenu().getViewer().closeInventory());
        } catch (IOException e) {
            getMenu().getViewer().sendMessage("§cCorrupt character file (Contact an Admin!)");
            e.printStackTrace();
        }
    }
}
