package me.blutkrone.rpgcore.nms.api.menu;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

/**
 * A chest menu for players to interact with.
 */
public interface IChestMenu {

    /**
     * Retrieve a list of elements that are contained within the
     * viewport, do note
     *
     * @param offset how far to offset within the viewport
     * @param viewport how big the viewport is
     * @param elements which elements are in the viewport
     * @return a list the length of viewport, if there are not enough
     *         elements available we pad it up with null values.
     */
    static <K> List<K> getViewport(int offset, int viewport, List<K> elements) {
        // retrieve the elements within our constraint
        List<K> viewing = new ArrayList<>();
        if (elements.size() <= viewport) {
            viewing.addAll(elements);
        } else {
            // ensure that the offset does not exit the viewport
            offset = Math.max(0, Math.min(elements.size() - viewport, offset));
            // retrieve the elements from within our viewport
            for (int i = offset; i < offset+viewport; i++) {
                viewing.add(elements.get(i));
            }
        }
        // pad up the viewport with what is missing
        while (viewing.size() < viewport)
            viewing.add(null);
        // offer up the padded viewport
        return viewing;
    }

    /**
     * Retrieve a list of elements that are contained within the
     * viewport, the keying refers to the position within the
     * original list.
     *
     * @param offset how far to offset within the viewport
     * @param viewport how big the viewport is
     * @param elements which elements are in the viewport
     * @return a list the length of viewport, if there are not enough
     *         elements available we pad it up with null values.
     */
    static <K> SortedMap<Integer, K> getIndexedViewport(int offset, int viewport, List<K> elements) {
        // retrieve the elements within our constraint
        SortedMap<Integer, K> viewing = new TreeMap<>();
        if (elements.size() <= viewport) {
            for (int i = 0; i < elements.size(); i++) {
                viewing.put(i, elements.get(i));
            }
        } else {
            // ensure that the offset does not exit the viewport
            offset = Math.max(0, Math.min(elements.size() - viewport, offset));
            // retrieve the elements from within our viewport
            for (int i = offset; i < offset+viewport; i++) {
                viewing.put(i, elements.get(i));
            }
        }
        // offer up the padded viewport
        return viewing;
    }

    /**
     * Clamp the given 'offset' so that that the pointer does not
     * exceed the elements.
     *
     * @param offset how far to offset within the viewport
     * @param viewport how big the viewport is
     * @param elements which elements are in the viewport
     * @return a list the length of viewport, if there are not enough
     *         elements available we pad it up with null values.
     */
    static int clampInViewport(int offset, int viewport, List<?> elements) {
        return Math.max(0, Math.min(elements.size()-viewport, offset));
    }

    /**
     * A brand is a persistent data tag allocated to the item.
     *
     * @param item which item receives the tag
     * @param plugin which plugin creates the tag
     * @param id the key for the brand
     * @param brand the value for the brand
     */
    static void setBrand(ItemStack item, JavaPlugin plugin, String id, String brand) {
        // ensure the item can actually hold a brand
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        // write the brand on the item
        NamespacedKey key = new NamespacedKey(plugin, "chest_menu_brand_"+id);
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(key, PersistentDataType.STRING, brand);
        item.setItemMeta(meta);
    }

    /**
     * A brand is a persistent data tag allocated to the item.
     *
     * @param item which item has the tag
     * @param plugin which plugin created the tag
     * @param id the key for the brand
     * @param defaults fallback in case no brand is set
     * @return the brand, else the default value
     */
    static String getBrand(ItemStack item, JavaPlugin plugin, String id, String defaults) {
        // ensure the item can actually hold a brand
        if (item == null || item.getType().isAir()) {
            return defaults;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return defaults;
        }
        // write the brand on the item
        NamespacedKey key = new NamespacedKey(plugin, "chest_menu_brand_" + id);
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(key, PersistentDataType.STRING, defaults);
    }

    /**
     * Who created and is viewing this menu.
     *
     * @return player related to menu
     */
    Player getViewer();

    /**
     * Execute an action with a brief delay.
     *
     * @param runnable the action to execute with a delay.
     */
    void stalled(Runnable runnable);

    /**
     * Clear all items in the menu.
     */
    void clearItems();

    /**
     * Rebuild the menu at the start of the next tick.
     *
     */
    void rebuild();

    /**
     * Write data to this interface, do note that the data will
     * persist so long the <b>instance</b> hasn't changed.
     *
     * @param key   keying to be retrieved.
     * @param value parameter that was saved.
     */
    void setData(String key, Object value);

    /**
     * Rebuild the menu, fetching up-to-date data. This is intended for
     * visible data only, the backing structure shouldn't change.
     *
     * @param rebuilder how to rebuild the menu
     */
    void setRebuilder(Runnable rebuilder);

    /**
     * Retrieve the data stored to this interface, do note that
     * the data will persist so long the <b>instance</b> hasn't
     * changed.
     * <p>
     * Do note that the default value also will define the class
     * constraint of the return value.
     *
     * @param key      keying to be retrieved
     * @param defaults parameter to default to
     * @return the data we had stored, or the default value.
     */
    <K> K getData(String key, K defaults);

    /**
     * Retrieve the data stored to this interface, do note that
     * the data will persist so long the <b>instance</b> hasn't
     * changed.
     *
     * @param key keying to be retrieved
     * @return the data we had stored, or null
     */
    <K> K getData(String key);

    /**
     * Provide a certain title, do note that this method should always
     * be called when the inventory is opened otherwise we may see the
     * fallback title.
     *
     * @param title  the title presented
     */
    void setTitle(BaseComponent[] title);

    /**
     * Delegate the logic on ticking the chest-gui to the handler, do note
     * that we are only ticked so long the menu is actually opened up.
     *
     * @param ticking_handler who we delegate the logic to
     */
    void setTickingHandler(Runnable ticking_handler);

    /**
     * Delegate the logic on handling the inventory logic, do note that
     * we are always supplied at low event priority.
     *
     * @param handler who we delegate the logic to
     */
    void setClickHandler(Consumer<InventoryClickEvent> handler);

    /**
     * Delegate the logic on handling the inventory logic, do note that
     * we are always supplied at low event priority.
     *
     * @param handler who we delegate the logic to
     */
    void setDragHandler(Consumer<InventoryDragEvent> handler);

    /**
     * Delegate the logic on handling the inventory logic, do note that
     * we are always supplied at low event priority.
     *
     * @param handler who we delegate the logic to
     */
    void setOpenHandler(Consumer<InventoryOpenEvent> handler);

    /**
     * Delegate the logic on handling the inventory logic, do note that
     * we are always supplied at low event priority.
     *
     * @param handler who we delegate the logic to
     */
    void setCloseHandler(Consumer<InventoryCloseEvent> handler);

    /**
     * Update the item at a certain slot
     *
     * @param slot slot to be updated
     * @param item item to use
     */
    void setItemAt(int slot, ItemStack item);

    /**
     * Retrieve the item at a certain slot.
     *
     * @param slot slot to retrieve from
     * @return item retrieved
     */
    ItemStack getItemAt(int slot);

    /**
     * Open the chest menu to the player who initiated it.
     */
    void open();
}
