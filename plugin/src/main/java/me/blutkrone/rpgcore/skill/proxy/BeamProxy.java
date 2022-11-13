package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A beam is capable of rotation, trying to hit an entity
 * at the given distance.
 */
public class BeamProxy extends AbstractSkillProxy {

    private static int BEAM_PROXY_INTERVAL = 2;

    private boolean terminate = false;
    private int cycle = 0;
    private String cooldown_uid;

    // anchor of the beam
    private IOrigin anchor;
    // non-freestyle rotates arbitrarily
    private float rotation_offset;
    private float rotation_per_tick;
    // beam is static, but can face any direction
    private boolean freestyle;
    // range of the beam
    private double current_range;
    private double range_per_tick;
    private double maximum_range;
    // visual entity at the spawnpoint
    private IEntityVisual item_entity;
    // beam impact invocation
    private MultiMechanic impact;
    // effect along the beam line
    private List<String> beam_effects;
    // effect at beam pointer
    private List<String> head_effects;
    // cooldown before applying beam again
    private int cooldown;
    // time limit of the beam
    private int duration;
    // filter on who can be hit
    private List<AbstractCoreSelector> filter;

    /**
     * A beam is capable of rotation, trying to hit an entity
     * at the given distance.
     *
     * @param context the context provided by the skill
     * @param origin location to anchor proxy at
     * @param item item spawned at the anchor
     * @param rotation_offset offset while non-freestyle for rotation
     * @param rotation_per_second degrees rotated per second
     * @param freestyle no rotation, but arbitrary direction
     * @param current_range original range of the beam
     * @param range_per_second beam range gained per second
     * @param maximum_range maximum distance beam expands to
     * @param impact entity which is affected by the beam
     * @param beam_effects scattered along beam line
     * @param head_effects effect only at the tip of the beam
     * @param cooldown interval to update beam at
     * @param filter filter on who can be hit by beam
     */
    public BeamProxy(IContext context, IOrigin origin, ItemStack item, float rotation_offset, float rotation_per_second, boolean freestyle, double current_range, double range_per_second,
                     double maximum_range, MultiMechanic impact, List<String> beam_effects, List<String> head_effects, int cooldown, int duration,
                     List<AbstractCoreSelector> filter) {
        super(context);

        this.anchor = origin.isolate();
        this.rotation_offset = rotation_offset;
        this.rotation_per_tick = (rotation_per_second / 20f) * BeamProxy.BEAM_PROXY_INTERVAL;
        this.freestyle = freestyle;
        this.current_range = current_range;
        this.range_per_tick = (range_per_second / 20f) * BeamProxy.BEAM_PROXY_INTERVAL;
        this.maximum_range = maximum_range;
        this.impact = impact;
        this.beam_effects = beam_effects;
        this.head_effects = head_effects;
        this.cooldown = cooldown;
        this.duration = duration;
        this.filter = filter;
        this.cooldown_uid = "BEAM_PROXY_" + UUID.randomUUID().toString().toUpperCase().replace("-", "");

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
        if (this.cycle++ % BeamProxy.BEAM_PROXY_INTERVAL == 0) {
            return false;
        }
        // expand the beam distance appropriately
        this.current_range += this.range_per_tick;
        this.current_range = Math.min(this.current_range, this.maximum_range);
        this.rotation_offset += this.rotation_per_tick;

        Location location = this.anchor.getLocation().clone();
        if (!this.freestyle) {
            // non-freestyle rotates, but is fixed on XZ axis
            location.setPitch(0);
            location.setYaw(this.rotation_offset);
        }

        // ray-trace to hit a block or entity
        Vector direction = location.getDirection();
        RayTraceResult trace_result = location.getWorld().rayTrace(location, direction, this.current_range, FluidCollisionMode.NEVER,
                true, 0.1d, (e -> {
                    CoreEntity core_entity = RPGCore.inst().getEntityManager().getEntity(e.getUniqueId());
                    if (core_entity == null || getContext().getCoreEntity() == core_entity) {
                        return false;
                    }

                    List<IOrigin> filtered = Collections.singletonList(core_entity);
                    for (AbstractCoreSelector filter : this.filter) {
                        filtered = filter.doSelect(getContext(), filtered);
                    }
                    return !filtered.isEmpty();
                }));

        if (trace_result != null) {
            // constrain length of beam to total distance
            this.current_range = location.toVector().distance(trace_result.getHitPosition());
            // impact on entity we've hit
            if (trace_result.getHitEntity() != null) {
                Entity entity = trace_result.getHitEntity();
                CoreEntity core_entity = RPGCore.inst().getEntityManager().getEntity(entity.getUniqueId());
                if (core_entity != null && core_entity.getCooldown(this.cooldown_uid) <= 0) {
                    core_entity.setCooldown(this.cooldown_uid, this.cooldown);
                    this.impact.doMechanic(getContext(), Collections.singletonList(core_entity));
                }
            }
        }

        // trace out the beam on the map
        visualizeLine(location.clone(), this.current_range);
        visualizeHead(location.clone(), this.current_range);

        return false;
    }

    @Override
    public void pleaseCancelThis() {
        this.terminate = true;
    }

    /*
     * Renders random effects along the line at a 0.5 unit distance.
     *
     * @param location anchor to render from
     * @param distance range to cover
     */
    private void visualizeLine(Location location, double distance) {
        if (!this.beam_effects.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                // always invoke effect at anchor position
                String effect_id = this.beam_effects.get(ThreadLocalRandom.current().nextInt(this.beam_effects.size()));
                CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
                effect.show(location);
                // spread effect at 0.5 interval over the line
                double remaining = distance;
                while (remaining > 0d) {
                    Vector direction = location.getDirection().clone();
                    location.add(direction.multiply(Math.min(0.5d, remaining)));
                    effect_id = this.beam_effects.get(ThreadLocalRandom.current().nextInt(this.beam_effects.size()));
                    effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
                    effect.show(location);
                    remaining -= 0.5d;
                }
            });
        }
    }

    /*
     * Renders random effects at start and finish
     *
     * @param location anchor to render from
     * @param distance range to cover
     */
    private void visualizeHead(Location location, double distance) {
        if (!this.head_effects.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                // always invoke effect at anchor position
                String effect_id = this.head_effects.get(ThreadLocalRandom.current().nextInt(this.head_effects.size()));
                CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
                effect.show(location);
                // spread effect at 0.5 interval over the line
                effect_id = this.head_effects.get(ThreadLocalRandom.current().nextInt(this.head_effects.size()));
                effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
                effect.show(location.clone().add(location.getDirection().clone().multiply(distance)));
            });
        }
    }
}
