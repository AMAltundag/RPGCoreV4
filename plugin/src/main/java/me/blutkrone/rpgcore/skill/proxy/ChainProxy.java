package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Instantly moves between entities so long there is
 * a line-of-sight, delay happens between chains.
 */
public class ChainProxy extends AbstractSkillProxy {

    private IOrigin anchor;
    private boolean terminate = false;
    private Set<UUID> blacklist;
    private MultiMechanic impact;
    private List<String> effects;
    private int cycle;
    private int chains;
    private int delay;
    private double radius;
    private List<AbstractCoreSelector> filter;

    /**
     * Instantly moves between entities so long there is
     * a line-of-sight, delay happens between chains.
     *
     * @param context the context provided by the skill
     * @param origin  location to anchor proxy at
     * @param impact  logic invoked upon impact
     * @param effects cosmetic effects to highlight effect
     * @param chains  how often we can chain
     * @param delay   delay between chains
     * @param radius  distance we can chain between
     * @param filter  filter atop the radius
     */
    public ChainProxy(IContext context, IOrigin origin, MultiMechanic impact, List<String> effects, int chains, int delay, double radius, List<AbstractCoreSelector> filter) {
        super(context);

        this.blacklist = new HashSet<>();
        this.anchor = origin.isolate();
        this.impact = impact;
        this.effects = effects;
        this.chains = chains;
        this.delay = Math.max(1, delay);
        this.radius = radius;
        this.filter = filter;
    }

    @Override
    public boolean update() {
        // early termination
        if (this.terminate || this.cycle > 200 || this.chains < 0) {
            return true;
        }
        // limit execution to interval
        if (this.cycle++ % this.delay == 0) {
            return false;
        }
        // identify candidates
        List<CoreEntity> nearby = this.anchor.getNearby(this.radius);
        nearby.removeIf(n -> this.blacklist.contains(n.getUniqueId()));
        if (getContext().getCoreEntity() != null) {
            nearby.remove(getContext().getCoreEntity());
        }
        List<IOrigin> filtered = new ArrayList<>(nearby);
        for (AbstractCoreSelector selector : this.filter) {
            filtered = selector.doSelect(getContext(), filtered);
        }
        // no targets = finished
        if (filtered.isEmpty()) {
            this.terminate = true;
            return false;
        }

        // sort candidates by distance
        filtered.sort(Comparator.comparingDouble(o -> o.distance(this.anchor)));
        // chain to first linked candidate
        for (IOrigin targeted : filtered) {
            if (targeted.hasLineOfSight(this.anchor)) {
                // update the anchor
                this.anchor = targeted.isolate();
                // do not chain off same entity
                if (targeted instanceof CoreEntity) {
                    this.blacklist.add(((CoreEntity) targeted).getUniqueId());
                }
                // update position we anchor from
                Location position = this.anchor.getLocation();
                position.setDirection(targeted.getLocation().toVector().subtract(position.toVector()).normalize());
                visualize(position);
                // apply impact effect
                this.impact.doMechanic(getContext(), Collections.singletonList(targeted));
                // pop one chain off
                this.chains -= 1;
                // only one chain per tick invoked
                break;
            }
        }

        // check later if we can still chain
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
    private void visualize(Location location) {
        if (!this.effects.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                // always invoke effect at anchor position
                String effect_id = this.effects.get(ThreadLocalRandom.current().nextInt(this.effects.size()));
                CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
                effect.show(location);
            });
        }
    }
}
