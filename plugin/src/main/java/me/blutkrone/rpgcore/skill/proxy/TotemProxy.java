package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.entity.entities.CoreTotem;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates an immobile entity which can be inflicted with
 * damage. All skill logic is invoked by the creator. The
 * proxy is removed when the entity dies.
 * <br>
 * A totem may be moved, which in turn can move the proxy.
 */
public class TotemProxy extends AbstractSkillProxy {

    private int cycle;
    // the anchor of the totem entity
    private UUID anchor;
    // snapshot is kept for death cast
    private IOrigin last_anchor;
    // logic invoked while totem is alive
    private MultiMechanic logic_on_tick;
    // logic invoked upon finish (ran on creator)
    private MultiMechanic logic_on_finish;
    // limits duration of totem
    private int duration;
    // interval at which ticking logic is run
    private int interval;
    // totem is only active if filter is passed
    private List<AbstractCoreSelector> filter;

    /**
     * Creates an immobile entity which can be inflicted with
     * damage. All skill logic is invoked by the creator. The
     * proxy is removed when the entity dies.
     * <br>
     * A totem may be moved, which in turn can move the proxy.
     *
     * @param context         the context the area is created within.
     * @param origin          where the area is spawned at
     * @param interval        interval the logic is invoked at
     * @param duration        totem is removed after time passed
     * @param factory         physical entity that is spawned
     * @param logic_on_tick   logic invoked at the ticking rate
     * @param logic_on_finish logic invoked at the last tick
     */
    public TotemProxy(IContext context, IOrigin origin, int interval, int duration, EntityProvider factory, MultiMechanic logic_on_tick, MultiMechanic logic_on_finish, int health, List<AbstractCoreSelector> filter) {
        super(context);

        this.last_anchor = origin.isolate();
        this.logic_on_tick = logic_on_tick;
        this.logic_on_finish = logic_on_finish;
        this.interval = interval;
        this.duration = duration;
        this.filter = filter;

        LivingEntity bukkit_entity = factory.create(this.last_anchor.getLocation());
        if (bukkit_entity instanceof Mob) {
            ((Mob) bukkit_entity).setAware(false);
        } else {
            bukkit_entity.setAI(false);
        }

        CoreTotem totem = new CoreTotem(bukkit_entity, factory);
        totem.getAttribute("HEALTH_MAXIMUM").create(health);
        if (context.getCoreEntity() != null) {
            totem.setParent(context.getCoreEntity());
        }

        this.anchor = totem.getUniqueId();
        RPGCore.inst().getEntityManager().register(this.anchor, totem);
    }

    /**
     * Grab the totem entity backing up the proxy.
     *
     * @return the totem entity we are backed up by.
     */
    public CoreTotem getTotem() {
        return RPGCore.inst().getEntityManager().getTotem(this.anchor);
    }

    @Override
    public boolean update() {
        // snapshot the location as we deem appropriate
        CoreTotem totem = RPGCore.inst().getEntityManager().getTotem(this.anchor);
        if (totem != null) {
            LivingEntity entity = totem.getEntity();
            if (entity != null) {
                this.last_anchor = new IOrigin.SnapshotOrigin(entity.getEyeLocation());
            }
        }

        // early termination
        if (totem == null || totem.isInvalid() || this.cycle > this.duration) {
            this.logic_on_finish.doMechanic(getContext(), Collections.singletonList(this.last_anchor));
            if (totem != null) {
                RPGCore.inst().getEntityManager().unregister(totem.getUniqueId());
            }
            return true;
        }

        // limit execution to interval
        if (this.cycle++ % this.interval != 0) {
            return false;
        }

        // ensure that the totem can be invoked
        if (!this.filter.isEmpty()) {
            List<IOrigin> targets = Collections.singletonList(this.last_anchor);
            for (AbstractCoreSelector selector : this.filter) {
                targets = selector.doSelect(getContext(), targets);
            }
            if (targets.isEmpty()) {
                return false;
            }
            // spin totem to face a random target
            Location wanted = targets.get(ThreadLocalRandom.current().nextInt(targets.size())).getLocation();
            Location current = this.last_anchor.getLocation();
            Vector direction = wanted.clone().subtract(current).toVector().normalize();
            direction.setY(0d);
            current.setDirection(direction);
        }

        // random spin of totem
        totem.getEntity().setRotation((float) (Math.random() * 360f), 0f);

        // update location of the entity
        this.logic_on_tick.doMechanic(getContext(), Collections.singletonList(this.last_anchor));

        return false;
    }

    @Override
    public void pleaseCancelThis() {
        CoreTotem totem = RPGCore.inst().getEntityManager().getTotem(this.anchor);
        if (totem != null) {
            RPGCore.inst().getEntityManager().unregister(totem.getUniqueId());
        }
    }
}