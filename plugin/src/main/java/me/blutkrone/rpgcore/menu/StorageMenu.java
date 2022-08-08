package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.StoragePage;
import me.blutkrone.rpgcore.npc.trait.impl.CoreStorageTrait;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

// todo: this is better off as a scroller
@Deprecated
public class StorageMenu extends AbstractCoreMenu {

    private ItemStack invisible_item;
    private List<StoragePage> pages;
    private CoreNPC npc;

    public StorageMenu(CoreStorageTrait origin, CoreNPC npc) {
        super(6);
        this.invisible_item = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();
        this.pages = origin.pages;
        this.npc = npc;
    }

    @Override
    public void rebuild() {
        throw new UnsupportedOperationException("You cannot rebuild a storage menu!");
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!isRelevant(event.getCurrentItem())) {
            return;
        }

        // open the relevant storage we care about
        String storage_id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "storage-id", null);
        if (storage_id != null) {
            boolean expired = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "storage-expired", "0").equalsIgnoreCase("1");

            this.getMenu().stalled(() -> {
                // do not open empty expired storages
                CorePlayer _core_player = RPGCore.inst().getEntityManager().getPlayer(this.getMenu().getViewer());
                if (!_core_player.getStoredItems().containsKey("storage_" + storage_id) && expired) {
                    return;
                }
                // open the storage
                this.getMenu().getViewer().closeInventory();
                new Viewer(getMenu(), "storage_" + storage_id, expired).finish(this.getMenu().getViewer());
            });
        }
    }

    @Override
    public void open(InventoryOpenEvent event) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getPlayer());

        // put down the icons that can be rendered
        for (int i = 0; i < 54; i++) {
            if (i < this.pages.size()) {
                this.getMenu().setItemAt(i, this.pages.get(i).getIcon(core_player));
            } else {
                this.getMenu().setItemAt(i, invisible_item);
            }
        }

        // draw the storage menu design
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_storage"), ChatColor.WHITE);
        this.getMenu().setTitle(msb.compile());

        Bukkit.getLogger().severe("not implemented (scroller storage.)");
    }

    /*
     * Allows us to view a specific storage
     */
    private class Viewer extends AbstractCoreMenu {

        private IChestMenu parent;
        private String storage_id;
        private boolean expired;

        Viewer(IChestMenu parent, String storage_id, boolean expired) {
            super(6);
            this.parent = parent;
            this.storage_id = storage_id;
            this.expired = expired;
        }

        @Override
        public void rebuild() {
            throw new UnsupportedOperationException("You cannot rebuild a storage menu!");
        }

        @Override
        public void click(InventoryClickEvent event) {
            // restrict everything except removal if storage is expired
            if (this.expired) {
                event.setCancelled(true);

                if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
                    // instant retrieve while player can do so
                    if (event.getWhoClicked().getInventory().firstEmpty() != -1) {
                        ItemStack item = event.getCurrentItem();
                        if (item != null && !item.getType().isAir()) {
                            item = item.clone();
                            event.setCurrentItem(new ItemStack(Material.AIR));
                            event.getWhoClicked().getInventory().addItem(item);
                        }
                    }
                } else {
                    String message = language().getTranslation("storage_expired");
                    this.getMenu().getViewer().sendMessage(message);
                }
            }
        }

        @Override
        public void open(InventoryOpenEvent event) {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getPlayer());

            // dump stored items on the menu
            String b64_stored = core_player.getStoredItems().remove(storage_id);
            if (b64_stored != null) {
                try {
                    ItemStack[] loaded = BukkitSerialization.fromBase64(b64_stored);
                    for (int i = 0; i < 54; i++) {
                        this.getMenu().setItemAt(i, loaded[i]);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            // draw the storage menu design
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_storage"), ChatColor.WHITE);
            this.getMenu().setTitle(msb.compile());
        }

        @Override
        public void close(InventoryCloseEvent event) {
            // serialize now stored items again
            ItemStack[] contents = new ItemStack[54];
            for (int i = 0; i < 54; i++) {
                contents[i] = this.getMenu().getItemAt(i);
            }
            // check if storage is non-empty
            boolean empty = true;
            for (ItemStack content : contents) {
                if (content != null && !content.getType().isAir()) {
                    empty = false;
                }
            }
            // store non-empty storage only
            if (!empty) {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getPlayer());
                core_player.getStoredItems().put(storage_id, BukkitSerialization.toBase64(contents));
            }
            // open the storage list when closing
            suggestOpen(parent);
        }
    }
}
