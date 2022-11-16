package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Piercing projectile moving in a straight line.
 */
public class BoltProxy extends AbstractSkillProxy {

    private static int BOLT_PROXY_INTERVAL = 2;

    // contextual information
    private IOrigin anchor;
    private IEntityVisual item_entity;
    // proxy information
    private boolean terminate = false;
    private int cycle;
    private MultiMechanic impact;
    // area information
    private Set<UUID> blacklist;
    private List<String> effects;
    private int pierce;
    private double speed;
    private double radius;

    /**
     * Piercing projectile moving in a straight line.
     *
     * @param context the context provided by the skill
     * @param origin  location to anchor proxy at
     * @param item    item that marks the projectile
     * @param impact  logic invoked upon impact
     * @param effects cosmetic effects to highlight effect
     * @param pierce  entities we can pierce
     * @param speed   speed measured in blocks per second
     * @param radius  size of the projectile
     */
    public BoltProxy(IContext context, IOrigin origin, ItemStack item, MultiMechanic impact, List<String> effects, int pierce, double speed, double radius) {
        super(context);

        this.blacklist = new HashSet<>();
        this.anchor = origin.isolate();
        this.impact = impact;
        this.effects = effects;
        this.pierce = pierce;
        this.speed = speed / 20d * BoltProxy.BOLT_PROXY_INTERVAL;
        this.radius = radius;

        if (item != null) {
            this.item_entity = RPGCore.inst().getVolatileManager().createVisualEntity(origin.getLocation(), true);
            this.item_entity.setItem(EquipmentSlot.HAND, item);
        }
    }

    @Override
    public boolean update() {
        // early termination
        if (this.terminate || this.cycle > 200) {
            if (this.item_entity != null) {
                this.item_entity.remove();
            }
            return true;
        }
        // limit execution to interval
        if (this.cycle++ % BoltProxy.BOLT_PROXY_INTERVAL == 0) {
            return false;
        }
        // invoke impact on all entities within cast
        List<CoreEntity> impacted = this.anchor.rayCastEntities(this.speed, this.radius);
        impacted.removeIf(e -> this.blacklist.add(e.getUniqueId()));
        impacted.remove(getContext().getCoreEntity());
        if (!impacted.isEmpty()) {
            this.pierce -= impacted.size();
            this.impact.doMechanic(getContext(), new ArrayList<>(impacted));
            if (this.pierce <= 0) {
                this.terminate = true;
            }
        }
        // scatter effects along travel-line
        visualize(this.anchor.getLocation().clone(), this.speed);
        // update position
        Block block = this.anchor.rayCastBlock(this.speed).orElse(null);
        if (block == null || block.getType().isAir()) {
            // advance so long we did not impacted
            Location updated = anchor.getLocation();
            Vector direction = updated.getDirection();
            updated.add(direction.multiply(this.speed));
        } else {
            // we hit a block, so we are done
            this.terminate = true;
            // visualize the impact against the block
            this.impact.doMechanic(getContext(), Collections.singletonList(anchor.isolate()));
        }
        // re-locate projectile entity
        if (this.item_entity != null) {
            Location updated = anchor.getLocation();
            this.item_entity.move(updated.getX(), updated.getY(), updated.getZ(), updated.getPitch(), updated.getYaw());
        }
        // retain projectile
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
    private void visualize(Location location, double distance) {
        if (!this.effects.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                // always invoke effect at anchor position
                String effect_id = this.effects.get(ThreadLocalRandom.current().nextInt(this.effects.size()));
                CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
                effect.show(location);
                // spread effect at 0.5 interval over the line
                double remaining = distance;
                while (remaining > 0d) {
                    Vector direction = location.getDirection().clone();
                    location.add(direction.multiply(Math.min(0.5d, remaining)));
                    effect_id = this.effects.get(ThreadLocalRandom.current().nextInt(this.effects.size()));
                    effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
                    effect.show(location);
                    remaining -= 0.5d;
                }
            });
        }
    }
}
