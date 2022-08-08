package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.nms.api.menu.IMenuWrapper;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public abstract class AbstractCoreMenu implements IMenuWrapper {

    private int size;
    private IChestMenu menu;

    public AbstractCoreMenu(int size) {
        this.size = size;
    }

    @Override
    public IChestMenu getMenu() {
        if (this.menu == null) {
            throw new NullPointerException("Menu has not been finished!");
        }
        return this.menu;
    }

    @Override
    public final int getSize() {
        return this.size;
    }

    /*
     * Finish this menu and open it to the player.
     *
     * @param player who was this menu created for.
     */
    @Override
    public void finish(Player player) {
        if (this.menu != null) {
            throw new IllegalStateException("Menu was already finished!");
        }

        this.menu = RPGCore.inst().getVolatileManager().createMenu(getSize(), player);
        this.menu.setTickingHandler(this::tick);
        this.menu.setClickHandler(this::click);
        this.menu.setCloseHandler(this::close);
        this.menu.setOpenHandler(this::open);
        this.menu.setRebuilder(this::rebuild);
        this.menu.open();
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
