package me.blutkrone.rpgcore.nms.api.menu;

import org.bukkit.entity.Player;

public interface IInputWrapper {

    /**
     * Retrieve the backend instance, this should be provided with the
     * finishing method method.
     *
     * @return the backend instance.
     */
    ITextInput getMenu();

    /**
     * Invoked when the input finishes.
     *
     * @param text the input of the player
     */
    void response(String text);

    /**
     * Invoked when the player typed anything.
     *
     * @param text the input of the player
     */
    void update(String text);

    /**
     * Invoked when the menu is ticked.
     *
     * @param text the input of the player
     */
    void tick(String text);

    /**
     * Finish the construction of this menu, showing it to the
     * player and handling initialization.
     *
     * @param player who will receive the menu.
     * @param defaults what message to start with.
     */
    void finish(Player player, String defaults);
}
