package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A trap triggers when a target is close enough to it.
 */
public class TrapProxy extends AbstractSkillProxy {

    private static int TRAP_INTERVAL = 7;

    // contextual information
    private IOrigin anchor;
    private IEntityVisual item_entity;
    private int cycle;
    // proxy information
    private boolean terminate = false;
    private MultiMechanic impact;
    // anchor information
    private int duration;
    private double radius;
    private List<AbstractCoreSelector> filter;

    /**
     * Has a fixed location where a mechanic is invoked, until
     * the duration expires.
     *
     * @param context the context provided by the skill
     * @param origin location to anchor proxy at
     * @param item item that marks the projectile
     * @param impact ticked while the proxy is active
     * @param duration how many ticks the proxy lasts
     * @param radius trigger range for the trap
     */
    public TrapProxy(IContext context, IOrigin origin, ItemStack item, int duration, double radius, MultiMechanic impact, List<AbstractCoreSelector> filter) {
        super(context);

        this.anchor = origin;
        this.impact = impact;
        this.duration = duration;
        this.radius = radius;
        this.filter = filter;

        if (item != null) {
            this.item_entity = RPGCore.inst().getVolatileManager().createVisualEntity(origin.getLocation(), true);
            this.item_entity.setItem(EquipmentSlot.HAND, item);
        }
    }

    @Override
    public boolean update() {
        // early termination
        if (this.terminate || this.cycle > this.duration) {
            if (this.item_entity != null) {
                this.item_entity.remove();
            }
            return true;
        }
        // limit to ticking rate
        if (this.cycle++ % TrapProxy.TRAP_INTERVAL != 0) {
            return false;
        }
        // search for anyone within range
        List<IOrigin> nearby = new ArrayList<>(this.anchor.getNearby(this.radius));
        nearby.remove(getContext().getCoreEntity());
        for (AbstractCoreSelector selector : this.filter) {
            nearby = selector.doSelect(getContext(), nearby);
        }
        // if we find anyone, we can set off the trap
        if (!nearby.isEmpty()) {
            this.impact.doMechanic(getContext(), Collections.singletonList(this.anchor));
            this.terminate = true;
        }
        // trap is still waiting to be triggered
        return false;
    }

    @Override
    public void pleaseCancelThis() {
        this.terminate = true;
    }
}
