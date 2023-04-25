package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractYesNoMenu extends AbstractCoreMenu {

    private Set<Integer> YES_SLOTS = new HashSet<>(Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48));
    private Set<Integer> NO_SLOTS = new HashSet<>(Arrays.asList(32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53));
    private boolean agreed = false;

    public AbstractYesNoMenu() {
        super(6);
    }

    /**
     * The prompt contains the question to be asked.
     *
     * @return Question to ask the player.
     */
    public abstract List<String> getPrompt();

    /**
     * @param response
     */
    public abstract void handleResponse(boolean response);

    @Override
    public void rebuild() {
        getMenu().clearItems();

        // base texture
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_ask_player"), ChatColor.WHITE);

        // question to ask
        List<String> prompt = getPrompt();
        for (int i = 0; i < prompt.size(); i++) {
            msb.shiftToExact(10).append(prompt.get(i), "menu_text_" + (i + 1));
        }

        getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        if (YES_SLOTS.contains(event.getSlot())) {
            // mark as having accepted
            this.agreed = true;
            // close the prompt
            getMenu().stalled(() -> getMenu().getViewer().closeInventory());
        } else if (NO_SLOTS.contains(event.getSlot())) {
            // close the prompt
            getMenu().stalled(() -> getMenu().getViewer().closeInventory());
        }
    }

    @Override
    public void close(InventoryCloseEvent event) {
        // handle the prompt response
        handleResponse(this.agreed);
    }
}
