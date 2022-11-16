package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Circular area that can (optionally) expand, affecting entities
 * within distance.
 */
public class AreaProxy extends AbstractSkillProxy {

    private static int AREA_INTERVAL = 4;

    // contextual information
    private IOrigin anchor;
    private IEntityVisual item_entity;
    // proxy information
    private boolean terminate = false;
    private int cycle;
    private MultiMechanic impact;
    private MultiMechanic ticker;
    // area information
    private double inner_radius;
    private double outer_radius;
    private List<String> effects;
    private int cooldown;
    private int duration;
    private List<AbstractCoreSelector> filter;
    private String cooldown_uid;

    /**
     * Create an area which has a certain effect on entities that
     * reside within it.
     *
     * @param context      the context the area is created within.
     * @param origin       where the area is spawned at
     * @param item         item that marks the area
     * @param inner_radius inner limit of radius
     * @param outer_radius outer limit of radius
     * @param effects      effect to highlight area
     * @param cooldown     time before same entity can be affected again
     * @param impact       invoked when an entity can be affected
     * @param ticker       invoked while proxy is active
     * @param filter       filters entities we can affect
     */
    public AreaProxy(IContext context, IOrigin origin, ItemStack item, double inner_radius, double outer_radius, List<String> effects, int cooldown, int duration, MultiMechanic impact, MultiMechanic ticker, List<AbstractCoreSelector> filter) {
        super(context);
        this.anchor = origin.isolate();
        this.impact = impact;
        this.inner_radius = inner_radius;
        this.outer_radius = outer_radius;
        this.effects = effects;
        this.ticker = ticker;
        this.cooldown = cooldown;
        this.duration = duration;
        this.filter = filter;
        this.cooldown_uid = "AREA_PROXY_" + UUID.randomUUID().toString().toUpperCase().replace("-", "");

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
        // limit execution to interval
        if (this.cycle++ % AreaProxy.AREA_INTERVAL != 0) {
            return false;
        }
        // effect to highlight the area
        Location location = this.anchor.getLocation();
        if (!this.effects.isEmpty()) {
            String effect_id = this.effects.get(ThreadLocalRandom.current().nextInt(this.effects.size()));
            CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
            effect.show(location);
        }
        // invoke ticker while active
        this.ticker.doMechanic(getContext(), Collections.singletonList(this.anchor));
        // impact on every entity within range
        List<IOrigin> nearby = new ArrayList<>(this.anchor.getNearby(this.outer_radius));
        for (AbstractCoreSelector selector : this.filter) {
            nearby = selector.doSelect(getContext(), nearby);
        }
        nearby.removeIf(e -> e.distance(this.anchor) >= this.inner_radius);
        nearby.removeIf(e -> e.distance(this.anchor) <= this.outer_radius);
        nearby.removeIf(e -> ((CoreEntity) e).getCooldown(this.cooldown_uid) > 0);
        // affect the proxy and put on cooldown
        this.impact.doMechanic(getContext(), nearby);
        for (IOrigin origin : nearby) {
            ((CoreEntity) origin).setCooldown(this.cooldown_uid, cooldown);
        }

        return false;
    }

    @Override
    public void pleaseCancelThis() {
        this.terminate = true;
    }
}
