package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.entity.entities.CoreTotem;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.UUID;

/**
 * Creates an immobile entity which can be inflicted with
 * damage. All skill logic is invoked by the creator. The
 * proxy is removed when the entity dies.
 *
 * A totem may be moved, which in turn can move the proxy.
 */
public class TotemProxy extends AbstractSkillProxy {

    private int cycle;
    // the anchor of the totem entity
    private UUID anchor;
    private IOrigin last_anchor;
    // logic invoked while totem is alive
    private MultiMechanic logic_on_tick;
    // logic invoked upon finish (ran on creator)
    private MultiMechanic logic_on_finish;
    // limits duration of totem
    private int duration;
    // interval at which ticking logic is run
    private int interval;

    /**
     * Creates an immobile entity which can be inflicted with
     * damage. All skill logic is invoked by the creator. The
     * proxy is removed when the entity dies.
     *
     * A totem may be moved, which in turn can move the proxy.
     *
     * @param context the context the area is created within.
     * @param origin where the area is spawned at
     * @param interval interval the logic is invoked at
     * @param duration totem is removed after time passed
     * @param factory physical entity that is spawned
     * @param logic_on_tick logic invoked at the ticking rate
     * @param logic_on_finish logic invoked at the last tick
     */
    public TotemProxy(IContext context, IOrigin origin, int interval, int duration, EntityProvider factory, MultiMechanic logic_on_tick, MultiMechanic logic_on_finish, int health) {
        super(context);

        this.last_anchor = origin.isolate();
        this.logic_on_tick = logic_on_tick;
        this.logic_on_finish = logic_on_finish;
        this.interval = interval;
        this.duration = duration;

        LivingEntity bukkit_entity = factory.create(this.last_anchor.getLocation());
        CoreTotem totem = new CoreTotem(bukkit_entity, factory);
        totem.getAttribute("HEALTH_MAXIMUM").create(health);
        totem.setParent(context.getCoreEntity());
        this.anchor = totem.getUniqueId();
    }

    @Override
    public boolean update() {
        // snapshot the location as we deem appropriate
        CoreTotem totem = RPGCore.inst().getEntityManager().getTotem(this.anchor);
        if (totem != null) {
            this.last_anchor = totem.isolate();
        }
        // early termination
        if (totem == null || totem.isInvalid() || this.duration > this.cycle) {
            this.logic_on_finish.doMechanic(getContext(), Collections.singletonList(this.last_anchor));
            if (totem != null) {
                totem.remove();
            }
            return true;
        }
        // limit execution to interval
        if (this.cycle++ % this.interval != 0) {
            return false;
        }
        // update location of the entity
        this.logic_on_tick.doMechanic(getContext(), Collections.singletonList(totem));

        return false;
    }

    @Override
    public void pleaseCancelThis() {
        CoreTotem totem = RPGCore.inst().getEntityManager().getTotem(this.anchor);
        if (totem != null) {
            totem.remove();
        }
    }
}