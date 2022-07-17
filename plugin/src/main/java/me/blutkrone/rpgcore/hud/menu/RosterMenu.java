package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.data.IDataIdentity;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.hud.initiator.AliasInitiator;
import me.blutkrone.rpgcore.hud.initiator.ClassInitiator;
import me.blutkrone.rpgcore.hud.initiator.SpawnInitiator;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A menu where the user can pick which character from
 * their roster they wish to play on, or create another
 * character on their roster.
 */
public class RosterMenu {

    // 9 pages with 3 slots each are available
    private Page[] roster_pages = new Page[9];
    // initiators that customize the character
    private List<IRosterInitiator> initiators = new ArrayList<>();

    /**
     * A menu where the user can pick which character from
     * their roster they wish to play on, or create another
     * character on their roster.
     *
     * @throws IOException should the config file be bad.
     */
    public RosterMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "roster.yml"));

        // create the pages for the menu
        for (int i = 0; i < 9; i++) {
            this.roster_pages[i] = new Page(i, config.getSection("roster-page." + i));
        }

        // handle character configuration once spawned
        this.initiators.add(new ClassInitiator(config));
        this.initiators.add(new SpawnInitiator(config));
        this.initiators.add(new AliasInitiator(config));

        this.initiators.sort(Comparator.comparingInt(IRosterInitiator::priority));
    }

    /**
     * Check if the player has finished their initiation routine.
     *
     * @param player whose initiation status are we checking
     * @return true if we are finished
     */
    public boolean hasInitiated(CorePlayer player) {
        // ensure we haven't already initiated everything
        if (player.isInitiated()) {
            return true;
        }

        // wait until initiator can open a menu
        if (player.getEntity().getOpenInventory().getType() != InventoryType.CRAFTING) {
            return false;
        }

        // check if there is any initiator still pending
        for (IRosterInitiator initiator : initiators) {
            if (initiator.initiate(player)) {
                return false;
            }
        }

        // otherwise we are done
        player.setInitiated();

        // move to the last known position
        player.moveToLoginPosition();

        return true;
    }

    /**
     * Open the roster menu for the given player.
     *
     * @param _player who to present the roster menu to.
     */
    public void open(Player _player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, _player);
        menu.setData("current-page", 0);
        menu.setRebuilder((() -> {
            // clear out all items on the menu
            menu.clearItems();

            // updated msb title for the menu
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_roster"), ChatColor.WHITE);

            // create icons to swap between pages
            for (int i = 0; i < 9; i++) {
                Page page_icon = roster_pages[i];

                if (menu.getViewer().hasPermission(page_icon.permission)) {
                    menu.setItemAt(45 + i, page_icon.icon_available);
                } else {
                    menu.setItemAt(45 + i, page_icon.icon_locked);
                }
            }
            // create portraits of the current characters
            int current_page = menu.getData("current-page", 0);

            for (int i = 0; i < 3; i++) {
                String portrait = "nothing";

                // prepare default information
                ItemStack itemized = RPGCore.inst().getLanguageManager()
                        .getAsItem("empty_roster_slot", (current_page * 3) + i)
                        .persist("roster-slot", (current_page * 3) + i)
                        .build();

                // attempt to load character information, if we got any
                try {
                    Map<String, DataBundle> raw_data = RPGCore.inst().getDataManager().getRawData(IDataIdentity.of(menu.getViewer().getUniqueId(), current_page * 3 + i));

                    if (!raw_data.isEmpty()) {
                        String alias = raw_data.get("display").getString(0);
                        portrait = raw_data.get("display").getString(1);
                        itemized = ItemBuilder.of("IRON_AXE:9999")
                                .persist("roster-slot", current_page * 3 + i)
                                .name("§fSlot #" + current_page * 3 + i)
                                .appendLore("§fAlias: '" + alias + "'")
                                .appendLore("§fPortrait: '" + portrait + "'")
                                .build();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // set the tooltip items
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 4; k++) {
                        menu.setItemAt(k * 9 + (3 * i) + j, itemized);
                    }
                }

                // contribute to the title
                msb.shiftToExact(54 * i).append(rpm.texture("static_" + portrait + "_job_portrait"), ChatColor.WHITE);
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_roster"));
            instructions.apply(msb);

            // supply the title to the player
            menu.setTitle(msb.compile());
        }));
        menu.setClickHandler(click -> {
            click.setCancelled(true);
            if (click.getCurrentItem() == null)
                return;
            if (click.getCurrentItem().getType() == Material.AIR)
                return;
            ItemMeta meta = click.getCurrentItem().getItemMeta();
            if (meta == null)
                return;

            Player viewer = (Player) click.getWhoClicked();

            // check if we are doing a page swap
            for (int i = 0; i < roster_pages.length; i++) {
                Page page = roster_pages[i];
                if (page.icon_available.isSimilar(click.getCurrentItem())) {
                    menu.setData("current-page", i);
                    menu.rebuild();
                    return;
                }
            }

            // otherwise check if we are trying to join on our character
            PersistentDataContainer data = meta.getPersistentDataContainer();
            int slot = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "roster-slot"), PersistentDataType.INTEGER, -1);
            if (slot < 0) return;

            // joining should create the character for us
            try {
                CorePlayer core_player = RPGCore.inst().getDataManager().loadPlayer(viewer, IDataIdentity.of(viewer.getUniqueId(), slot));
                RPGCore.inst().getEntityManager().register(viewer.getUniqueId(), core_player);
                viewer.closeInventory();
            } catch (IOException e) {
                viewer.sendMessage("§cCorrupt character file (Contact an Admin!)");
                e.printStackTrace();
            }
        });
        menu.open();
    }

    /*
     * A page which can contain 3 characters.
     */
    class Page {
        // icon to show if the page is available
        final ItemStack icon_available;
        // icon to show if the page is locked
        final ItemStack icon_locked;
        // permission to gate the page behind
        final String permission;

        Page(int i, ConfigWrapper config) {
            // icon to use while slot is unlocked
            this.icon_available = RPGCore.inst().getLanguageManager().getAsItem(config.getString("available"), i)
                    .persist("slot_id", i).build();
            this.icon_locked = RPGCore.inst().getLanguageManager().getAsItem(config.getString("locked"), i)
                    .persist("slot_id", i).build();
            // permission to gate page behind
            this.permission = i == 0 ? "" : config.getString("permission", "");
        }
    }

}
