package me.blutkrone.rpgcore.api.item;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.inventory.ItemStack;

public interface IItemDescriber {
    /**
     * Describe an item within RPGCore specification, do note that if we
     * are called by non RPGCore items ignore this call.
     *
     * @param item   the item to describe.
     * @param player
     */
    void describe(ItemStack item, CorePlayer player);
}
