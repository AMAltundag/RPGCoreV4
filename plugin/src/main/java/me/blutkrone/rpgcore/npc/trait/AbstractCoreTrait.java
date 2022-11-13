package me.blutkrone.rpgcore.npc.trait;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.npc.AbstractEditorNPCTrait;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.CoreNPC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * A trait provides logic to a NPC,
 */
public abstract class AbstractCoreTrait {

    private String unlock;
    private String icon_lc;
    private String symbol;
    private UUID uuid;

    public AbstractCoreTrait(AbstractEditorNPCTrait editor) {
        this.symbol = editor.getCortexSymbol();
        this.icon_lc = editor.getIconLC();
        this.uuid = UUID.randomUUID();
        this.unlock = editor.getUnlockFlag().toLowerCase();
    }

    /**
     * Check if the trait is available to a player.
     *
     * @param player who to check against.
     * @return true if available
     */
    public boolean isAvailable(CorePlayer player) {
        // always show default traits
        if (this.getUnlock().equalsIgnoreCase("quest_tag_none")) {
            return true;
        }
        // non-default trait should check player tags
        if (player.getPersistentTags().contains(getUnlock())) {
            return true;
        }
        // not available otherwise
        return false;
    }

    /**
     * Retrieve the flag necessary to unlock this trait.
     *
     * @return the player flag/tag necessary to unlock this trait.
     */
    public String getUnlock() {
        return "quest_tag_" + this.unlock;
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
    public ItemStack getIcon() {
        ItemStack build = RPGCore.inst().getLanguageManager().getAsItem(this.icon_lc).build();
        IChestMenu.setBrand(build, RPGCore.inst(), "cortex-id", String.valueOf(this.uuid));
        return build;
    }

    /**
     * Retrieve a unique ID assigned arbitrarily when instantiated, this is
     * not meant a persistent value!
     *
     * @return a temporary identifier for the trait.
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * Player wants to engage with this core trait.
     *
     * @param player who wants to engage with it.
     * @param npc
     */
    public abstract void engage(Player player, CoreNPC npc);
}
