package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.root.npc.EditorStorageTrait;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.StoragePage;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Storage for items shared across the roster.
 */
public class CoreStorageTrait extends AbstractCoreTrait {

    private List<StoragePage> pages = new ArrayList<>();

    public CoreStorageTrait(EditorStorageTrait editor) {
        super(editor);
        for (String s : editor.stored) {
            StoragePage storage = RPGCore.inst().getNPCManager().getStorage(s);
            if (storage != null) {
                this.pages.add(storage);
            }
        }
    }

    @Override
    public void engage(Player player) {
        if (RPGCore.inst().getEntityManager().getPlayer(player) == null) {
            return;
        }
        if (this.pages.isEmpty()) {
            player.sendMessage("§cINTERNAL ERROR - STORAGE PAGES NOT FOUND!");
            return;
        }

        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        ItemStack invisible_item = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player);
        menu.setOpenHandler((e) -> {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(e.getPlayer());

            // put down the icons that can be rendered
            for (int i = 0; i < 54; i++) {
                if (i < this.pages.size()) {
                    menu.setItemAt(i, this.pages.get(i).getIcon(core_player));
                } else {
                    menu.setItemAt(i, invisible_item);
                }
            }

            // draw the storage menu design
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_storage"), ChatColor.WHITE);
            menu.setTitle(msb.compile());
        });
        menu.setClickHandler((e) -> {
            e.setCancelled(true);

            // open the relevant storage we care about
            String brand = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "storage-id", null);
            if (brand != null) {
                boolean stored = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "storage-expired", "0").equalsIgnoreCase("1");
                menu.stalled(() -> openPage(((Player) e.getWhoClicked()), "storage_" + brand, stored));
            }
        });
        menu.setRebuilder(() -> {
            Bukkit.getLogger().severe("ILLEGAL REBUILD - STORAGE CANNOT DO THIS!");
        });
        menu.open();
    }

    /*
     * Open the contents of a page, once closed open the core page
     * up again.
     *
     * @param bukkit_player whose storage do we open
     * @param page what page do we open
     * @param expired if expired, only allow removal
     */
    private void openPage(Player bukkit_player, String page, boolean expired) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        // do not open empty storages if they are not active
        CorePlayer _core_player = RPGCore.inst().getEntityManager().getPlayer(bukkit_player);
        if (!_core_player.getStoredItems().containsKey(page) && expired) {
            return;
        }

        // open a storage for this particular page
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, bukkit_player);
        menu.setOpenHandler((e) -> {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(e.getPlayer());

            // dump stored items on the menu
            String b64_stored = core_player.getStoredItems().remove(page);
            if (b64_stored != null) {
                try {
                    ItemStack[] loaded = BukkitSerialization.fromBase64(b64_stored);
                    for (int i = 0; i < 54; i++) {
                        menu.setItemAt(i, loaded[i]);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            // draw the storage menu design
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_storage"), ChatColor.WHITE);
            menu.setTitle(msb.compile());
        });
        menu.setCloseHandler(e -> {
            // serialize now stored items again
            ItemStack[] contents = new ItemStack[54];
            for (int i = 0; i < 54; i++) {
                contents[i] = menu.getItemAt(i);
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
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(e.getPlayer());
                core_player.getStoredItems().put(page, BukkitSerialization.toBase64(contents));
            }
            // open the containing menu when closing
            menu.stalled(() -> engage(menu.getViewer()));
        });
        menu.setRebuilder(() -> {
            Bukkit.getLogger().severe("ILLEGAL REBUILD - STORAGE CANNOT DO THIS!");
        });

        menu.open();
    }
}
