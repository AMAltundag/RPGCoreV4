package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A trap triggers when a target is close enough to it.
 */
public class TrapProxy extends AbstractSkillProxy {

    private static int TRAP_INTERVAL = 7;

    // initial trap is "thrown" to find its location
    private ArmorStand thrown_entity;
    private ItemStack item;
    // afterwards it becomes stationary
    private IEntityVisual item_entity;

    // contextual information
    private IOrigin anchor;
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

        this.anchor = origin.isolate();
        this.impact = impact;
        this.duration = duration;
        this.radius = radius;
        this.filter = filter;
        this.item = item;

        if (item != null) {
            Vector direction = anchor.getLocation().getDirection();
            this.anchor.getLocation().setDirection(new Vector(Math.random()*2-1, 0d, Math.random()*2-1));

            this.thrown_entity = (ArmorStand) anchor.getLocation().getWorld().spawnEntity(this.anchor.getLocation(), EntityType.ARMOR_STAND);
            this.thrown_entity.setInvisible(true);
            this.thrown_entity.setInvulnerable(true);
            this.thrown_entity.setSmall(false);
            this.thrown_entity.setArms(false);
            this.thrown_entity.setRightArmPose(new EulerAngle(0f, 0f, 0f));
            this.thrown_entity.setLeftArmPose(new EulerAngle(0f, 0f, 0f));
            this.thrown_entity.setBasePlate(false);
            this.thrown_entity.setPersistent(false);
            this.thrown_entity.setRemoveWhenFarAway(false);
            this.thrown_entity.getEquipment().setItemInMainHand(item);

            Vector random = new Vector(Math.random()*2-1,Math.random()*2-1,Math.random()*2-1);
            this.thrown_entity.setVelocity(direction.multiply(0.75).add(random.multiply(0.15d)).add(new Vector(0.0d, 0.6d, 0.0)));
        }
    }

    @Override
    public boolean update() {
        // handle "thrown" phase
        if (this.thrown_entity != null) {
            if (this.terminate) {
                this.thrown_entity.remove();
                this.thrown_entity = null;
            } else if (this.thrown_entity.getVelocity().distanceSquared(new Vector()) <= 0.03d) {
                if (!this.thrown_entity.getLocation().add(0d, -0.01d, 0d).getBlock().isPassable()) {
                    if (this.item != null) {
                        this.anchor = new IOrigin.SnapshotOrigin(this.thrown_entity.getLocation());
                        this.item_entity = RPGCore.inst().getVolatileManager().createVisualEntity(this.anchor.getLocation(), false);
                        this.item_entity.setItem(EquipmentSlot.HAND, this.item);
                    }

                    this.thrown_entity.remove();
                    this.thrown_entity = null;
                }
            }

            return false;
        }

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
            // spin trap to face a random target
            Location wanted = nearby.get(ThreadLocalRandom.current().nextInt(nearby.size())).getLocation();
            Location current = this.anchor.getLocation();
            Vector direction = wanted.clone().subtract(current).toVector().normalize();
            direction.setY(0d);
            current.setDirection(direction);
            // invoke the logic of the mechanic
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
