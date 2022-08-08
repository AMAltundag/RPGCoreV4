package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.nms.api.menu.IInputWrapper;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public abstract class AbstractCoreInput implements IInputWrapper {

    private ITextInput menu;

    @Override
    public ITextInput getMenu() {
        if (this.menu == null) {
            throw new NullPointerException("Menu has not been finished!");
        }
        return this.menu;
    }

    @Override
    public void finish(Player player, String defaults) {
        if (this.menu != null) {
            throw new IllegalStateException("Menu was already finished!");
        }
        if (defaults == null || defaults.isEmpty()) {
            defaults = " ";
        }

        // initialize the menu
        this.menu = RPGCore.inst().getVolatileManager().createInput(player);
        this.menu.setItemAt(0, language().getAsItem("invisible").name(defaults).build());
        this.menu.setUpdating(this::update);
        this.menu.setResponse(this::response);
        // apply default overlay
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-260).append(resourcepack().texture("menu_input_bad"), ChatColor.WHITE);
        this.menu.setTitle(msb.compile());
        this.menu.open();
    }

    @Override
    public void tick(String text) {

    }

    /*
     * Short annotation to access resourcepack manager.
     *
     * @return
     */
    protected ResourcePackManager resourcepack() {
        return RPGCore.inst().getResourcePackManager();
    }

    /*
     * Short annotation to access language manager.
     *
     * @return
     */
    protected LanguageManager language() {
        return RPGCore.inst().getLanguageManager();
    }

    /*
     * A suggestion to open means the menu opens, should no
     * other inventory be open. This is applied with a delay.
     *
     * @param menu the menu we are suggesting to open
     */
    protected void suggestOpen(IChestMenu menu) {
        menu.stalled(() -> {
            if (menu.getViewer().getOpenInventory().getType() == InventoryType.CRAFTING) {
                menu.open();
            }
        });
    }
}
