package me.blutkrone.rpgcore.npc;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.inventory.ItemStack;

/**
 * A configuration for a storage page.
 */
public class StoragePage {

    private String id;
    private String lc_unlocked;
    private String lc_locked;
    private String key;

    public StoragePage(String id, ConfigWrapper config) {
        this.id = id;
        this.lc_unlocked = config.getString("unlocked");
        this.lc_locked = config.getString("locked");
        this.key = config.getString("key");
    }

    /**
     * Retrieve the icon for this page, should the page be unlocked
     * it'll receive a 'storage-id' identifier.
     *
     * @param player whose storage do we check
     * @return icon for the storage
     */
    public ItemStack getIcon(CorePlayer player) {
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        if (this.key.equalsIgnoreCase("default")) {
            // default storage is always unlocked
            ItemStack item = lpm.getAsItem(this.lc_unlocked).build();
            IChestMenu.setBrand(item, RPGCore.inst(), "storage-id", this.id);
            return item;
        } else if (player.getStorageUnlocked().containsKey(this.key)) {
            long storage_unlocked_until = player.getStorageUnlocked().get(this.key);
            if (storage_unlocked_until == -1) {
                // storage is permanent
                ItemStack item = lpm.getAsItem(this.lc_unlocked).build();
                IChestMenu.setBrand(item, RPGCore.inst(), "storage-id", this.id);
                return item;
            } else if (storage_unlocked_until <= System.currentTimeMillis()) {
                // storage has expired
                ItemStack item = lpm.getAsItem(this.lc_unlocked)
                        .appendLore(lpm.getTranslation("storage_expired"))
                        .build();
                IChestMenu.setBrand(item, RPGCore.inst(), "storage-id", this.id);
                IChestMenu.setBrand(item, RPGCore.inst(), "expired", "1");
                return item;
            } else {
                // storage is on timer
                String expire_stamp = lpm.formatMillis(storage_unlocked_until - System.currentTimeMillis());
                ItemStack item = lpm.getAsItem(this.lc_unlocked)
                        .appendLore(lpm.getTranslation("storage_timer").replace("{TIME}", expire_stamp))
                        .build();
                IChestMenu.setBrand(item, RPGCore.inst(), "storage-id", this.id);
                return item;
            }


        } else {
            // icon used if the storage page is locked
            return RPGCore.inst().getLanguageManager().getAsItem(this.lc_locked).build();
        }
    }
}
