package me.blutkrone.rpgcore.npc.trait;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.root.npc.AbstractEditorNPCTrait;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A trait provides logic to a NPC,
 */
public abstract class AbstractCoreTrait {

    private String icon_lc;
    private String symbol;

    public AbstractCoreTrait(AbstractEditorNPCTrait editor) {
        this.symbol = editor.getCortexSymbol();
        this.icon_lc = editor.getIconLC();
    }

    /**
     * Retrieve the symbol which is rendered on the cortex.
     *
     * @return cortex symbol
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * An icon, intended to be invisible, to overlay atop the cortex.
     *
     * @return the cortex overlay item.
     */
    public ItemStack getIcon(int identifier) {
        ItemStack build = RPGCore.inst().getLanguageManager().getAsItem(this.icon_lc).build();
        IChestMenu.setBrand(build, RPGCore.inst(), "cortex-id", String.valueOf(identifier));
        return build;
    }

    /**
     * Player wants to engage with this core trait.
     *
     * @param player who wants to engage with it.
     */
    public abstract void engage(Player player);
}
