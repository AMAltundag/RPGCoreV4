package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

/**
 * Has a fixed location where a mechanic is invoked, until
 * the duration expires.
 */
public class AnchorProxy extends AbstractSkillProxy {

    private static int ANCHOR_INTERVAL = 4;

    // contextual information
    private IOrigin anchor;
    private ItemDisplay item_entity;
    // proxy information
    private boolean terminate = false;
    private int cycle;
    // anchor information
    private MultiMechanic ticker;
    private int duration;

    /**
     * Has a fixed location where a mechanic is invoked, until
     * the duration expires.
     *
     * @param context  the context provided by the skill
     * @param origin   location to anchor proxy at
     * @param item     item that marks the projectile
     * @param ticker   ticked while the proxy is active
     * @param duration how many ticks the proxy lasts
     */
    public AnchorProxy(IContext context, IOrigin origin, ItemStack item, MultiMechanic ticker, int duration) {
        super(context);

        this.anchor = origin.isolate();
        this.ticker = ticker;
        this.duration = duration;

        if (item != null) {
            this.item_entity = (ItemDisplay) origin.getWorld().spawnEntity(origin.getLocation(), EntityType.ITEM_DISPLAY);
            this.item_entity.setItemStack(item);
            this.item_entity.setBillboard(Display.Billboard.FIXED);
            this.item_entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        }
    }

    @Override
    public boolean update() {
        // early termination
        if (this.terminate || this.cycle > duration) {
            if (this.item_entity != null) {
                this.item_entity.remove();
            }
            return true;
        }
        // limit execution to interval
        if (this.cycle++ % AnchorProxy.ANCHOR_INTERVAL != 0) {
            return false;
        }
        // invoke ticker while active
        this.ticker.doMechanic(getContext(), Collections.singletonList(this.anchor));

        return false;
    }

    @Override
    public void pleaseCancelThis() {
        this.terminate = true;
    }
}
