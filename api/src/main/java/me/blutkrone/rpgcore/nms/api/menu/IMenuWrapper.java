package me.blutkrone.rpgcore.nms.api.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A wrapper which can be initialized by a volatile
 * manager as a chest menu. This is NOT meant to be
 * a shared handler. Each time this menu is to open
 * a new wrapper should be created.
 */
public interface IMenuWrapper {

    /**
     * Retrieve the backend instance, this should be provided with the
     * finishing method.
     *
     * @return the backend instance.
     */
    IChestMenu getMenu();

    /**
     * How many rows of slots for this menu.
     *
     * @return a number between 1 and 6
     */
    int getSize();

    /**
     * Invoked when building the menu.
     */
    void rebuild();

    /**
     * Invoked when there was a click with this menu.
     *
     * @param event the related bukkit event
     */
    void click(InventoryClickEvent event);

    /**
     * Invoked when the menu is ticked.
     */
    default void tick() {

    }

    /**
     * Invoked when the menu was opened.
     *
     * @param event the related bukkit event
     */
    default void open(InventoryOpenEvent event) {
        rebuild(); // force an instant rebuild by default
    }

    /**
     * Invoked when the menu was closed.
     *
     * @param event the related bukkit event
     */
    default void close(InventoryCloseEvent event) {

    }

    /**
     * Finish the construction of this menu, showing it to the
     * player and handling initialization.
     *
     * @param player who will receive the menu.
     */
    void finish(Player player);

    /**
     * An item is relevant if it can have non-trivial data.
     *
     * @param item the item to check.
     * @return true if relevant
     */
    default boolean isRelevant(ItemStack item) {
        // cannot be relevant if null
        if (item == null) {
            return false;
        }
        // cannot be relevant if air
        if (item.getType() == Material.AIR) {
            return false;
        }
        // cannot be relevant if meta-less
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        // relevant if we passed
        return true;
    }

    default boolean isUpperClick(InventoryClickEvent event) {
        return event.getClickedInventory() == event.getView().getTopInventory();
    }
}
