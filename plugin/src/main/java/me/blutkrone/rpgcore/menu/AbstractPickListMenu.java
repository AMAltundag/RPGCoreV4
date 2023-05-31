package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A menu allowing to pick from the given list of items.
 */
public abstract class AbstractPickListMenu extends AbstractCoreMenu {

    private Map<String, ItemStack> choices = new HashMap<>();
    private List<ItemStack> choices_listed = new ArrayList<>();
    private int offset;

    public AbstractPickListMenu() {
        super(6);
    }



    /**
     * Add a choice to the picklist.
     *
     * @param icon   How to render the option
     * @param choice Choice to present
     */
    public void addToList(ItemStack icon, String choice) {
        ItemStack copied = icon.clone();
        IChestMenu.setBrand(copied, RPGCore.inst(), "picklist", choice);
        choices.put(choice, copied);
        choices_listed.add(copied);
    }

    /**
     * Check if any choices were registered.
     *
     * @return Has any choices
     */
    public boolean hasChoices() {
        return !this.choices_listed.isEmpty();
    }

    /**
     * Feedback to inform about having picked.
     *
     * @param choice What choice was made.
     * @return Query to close menu
     */
    public abstract boolean onPick(String choice);

    /**
     * Whether to apply a highlight on the given choice
     * when it is rendered.
     *
     * @param choice What choice to check.
     * @return Highlight the choice.
     */
    public abstract boolean highlight(String choice);

    @Override
    public void rebuild() {
        this.getMenu().clearItems();

        // base texture
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_scroller_grid"), ChatColor.WHITE);

        // render viewport and place items
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                int k = (i + offset) * 8 + j;
                if (k < choices_listed.size()) {
                    // retrieve the item we are working with
                    ItemStack icon = choices_listed.get(k);
                    // highlight item that is affordable
                    String picklist = IChestMenu.getBrand(icon, RPGCore.inst(), "picklist", null);
                    if (highlight(picklist)) {
                        msb.shiftToExact(-2 + 18 * j);
                        msb.append(resourcepack().texture("scroller_highlight_" + i), ChatColor.WHITE);
                    }
                    // place clickable item we are using
                    this.getMenu().setItemAt(i * 9 + j, icon);
                }
            }
        }

        // render scroll-bar for the viewport
        msb.shiftToExact(150);
        if (choices_listed.size() <= 48) {
            msb.append(resourcepack().texture("pointer_huge_0"), ChatColor.WHITE);
        } else if (choices_listed.size() <= 96) {
            double length = Math.ceil(choices_listed.size() / 8d) - 6d;
            double ratio = offset / length;
            msb.append(resourcepack().texture("pointer_medium_" + (int) (100 * ratio)), ChatColor.WHITE);
        } else if (choices_listed.size() <= 192) {
            double length = Math.ceil(choices_listed.size() / 8d) - 6d;
            double ratio = offset / length;
            msb.append(resourcepack().texture("pointer_small_" + (int) (100 * ratio)), ChatColor.WHITE);
        } else {
            double length = Math.ceil(choices_listed.size() / 8d) - 6d;
            double ratio = offset / length;
            msb.append(resourcepack().texture("pointer_tiny_" + (int) (100 * ratio)), ChatColor.WHITE);
        }

        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getView().getTopInventory() == event.getClickedInventory()) {
            if (event.getSlot() == 8) {
                // scroll up by one
                this.offset = Math.max(0, this.offset - 1);
                this.getMenu().queryRebuild();
            } else if (event.getSlot() == 17) {
                // scroll to top
                this.offset = 0;
                this.getMenu().queryRebuild();
            } else if (event.getSlot() == 26) {
                // ignore other clicks
            } else if (event.getSlot() == 35) {
                // ignore other clicks
            } else if (event.getSlot() == 44) {
                // scroll to bottom
                this.offset = (choices_listed.size() / 8) - 6;
                this.getMenu().queryRebuild();
            } else if (event.getSlot() == 53) {
                // scroll down by one
                int floor = Math.max(0, (choices_listed.size() / 8) - 6);
                this.offset = Math.min(floor, this.offset + 1);
                this.getMenu().queryRebuild();
            } else {
                String picklist = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "picklist", null);
                if (picklist != null) {
                    if (onPick(picklist)) {
                        getMenu().stalled(() -> getMenu().getViewer().closeInventory());
                    }
                }
            }
        }
    }
}
