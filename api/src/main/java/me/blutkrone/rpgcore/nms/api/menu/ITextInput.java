package me.blutkrone.rpgcore.nms.api.menu;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Menu meant to fetch text based user input.
 */
public interface ITextInput {

    /**
     * Execute an action with a brief delay.
     *
     * @param runnable the action to execute with a delay.
     */
    void stalled(Runnable runnable);

    /**
     * Who created and is viewing this menu.
     *
     * @return player related to menu
     */
    Player getViewer();

    /**
     * Update the items at the given slot.
     *
     * @param slot which slot to update
     * @param item the resulting item
     */
    void setItemAt(int slot, ItemStack item);

    /**
     * Retrieve the item at the given slot.
     *
     * @param slot which slot to retrieve.
     * @return item on that slot, or air.
     */
    ItemStack getItemAt(int slot);

    /**
     * Establish a response protocol, the protocol will always
     * be called. A null value is passed, if the value fails a
     * validator check.
     *
     * @param response consumer to process input.
     */
    void setResponse(Consumer<String> response);

    /**
     * A method called each tick while the menu is open.
     *
     * @param ticker the ticking handler.
     */
    void setTicking(Consumer<String> ticker);

    /**
     * A method called if the input has changed.
     *
     * @param updater the update handler
     */
    void setUpdating(Consumer<String> updater);

    /**
     * Present this menu to the given player.
     */
    void open();

    /**
     * Provide a certain title, do note that this method should always
     * be called when the inventory is opened otherwise we may see the
     * fallback title.
     *
     * @param title the title presented
     */
    void setTitle(BaseComponent[] title);

    /**
     * Conclude this text input.
     */
    void conclude();

    /**
     * Update the content of this input.
     *
     * @param content updated content
     */
    void setCurrent(String content);
}
