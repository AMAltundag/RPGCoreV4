package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.mechanic.MultiMechanic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Expanding blast within a cone-like shape.
 */
public class BlastProxy extends AbstractSkillProxy {

    private static int BLAST_PROXY_INTERVAL = 4;
    private final double shrink_per_second;

    // contextual information
    private IOrigin anchor;
    // proxy information
    private boolean terminate = false;
    private int cycle;
    private MultiMechanic impact;
    // blast information
    private Set<UUID> blacklist;
    private double distance;
    private double expansion_per_second;
    private List<String> effects;
    private int duration;
    private double angle;
    private double up;

    /**
     * Expanding blast within a cone-like shape.
     *
     * @param context              the context provided by the skill
     * @param origin               location to anchor proxy at
     * @param impact               logic invoked upon impact
     * @param effects              cosmetic effects to highlight effect
     * @param duration             how many ticks the effect lasts
     * @param start                distance to start at
     * @param expansion_per_second expansion rate per second
     * @param angle                angle of the blast
     */
    public BlastProxy(IContext context, IOrigin origin, MultiMechanic impact, List<String> effects, int duration, double start, double expansion_per_second, int angle, double shrink_per_second) {
        super(context);

        this.anchor = origin.isolate();
        this.cycle = 0;
        this.blacklist = new HashSet<>();
        this.impact = impact;
        this.effects = effects;
        this.duration = duration;
        this.distance = start;
        this.expansion_per_second = expansion_per_second / 20 * BLAST_PROXY_INTERVAL;
        this.angle = Math.max(0, Math.min(angle, 180));
        this.shrink_per_second = shrink_per_second / 20 * BLAST_PROXY_INTERVAL;
    }

    /**
     * Applies a filter on the given subset of entities, the output
     * given is the
     *
     * @param pivot      the pivot to expand the cone from
     * @param radius_min the radius of the cone
     * @param radius_max the radius of the cone
     * @param angle      the angle of the cone
     * @param targets    the entities to filter
     * @return the targets within the cone shape
     */
    public static List<IOrigin> filter(IOrigin pivot, double radius_min, double radius_max, double angle, List<IOrigin> targets) {
        List<IOrigin> result = new ArrayList<>();

        // squared parameters perform better
        radius_min = radius_min * radius_min;
        radius_max = radius_max * radius_max;
        // turn pivot into vectors instead
        org.bukkit.util.Vector source = pivot.getLocation().toVector();
        org.bukkit.util.Vector direction = pivot.getLocation().getDirection();

        for (IOrigin target : targets) {
            // ensure we are in the same world
            if (target.getWorld() != pivot.getWorld()) {
                continue;
            }
            // ensure that entity is within cone radius
            Vector relative = target.getLocation().toVector().subtract(source);
            double length = relative.lengthSquared();
            if (length < radius_min || length > radius_max) {
                continue;
            }
            // ensure that entity is within cone angle
            if (Math.abs(Math.toDegrees(direction.angle(relative))) > angle) {
                continue;
            }
            // allow to retain this collection
            result.add(target);
        }

        return result;
    }

    @Override
    public boolean update() {
        // early termination
        if (this.terminate) {
            return true;
        }
        // limit execution to interval
        if (this.cycle++ % BlastProxy.BLAST_PROXY_INTERVAL == 0) {
            return false;
        }
        // extend the blast
        this.distance += this.expansion_per_second;
        this.angle = Math.max(15, this.angle - this.shrink_per_second);

        // slowly shrink angle during expand
        // search for new affected entities
        List<CoreEntity> entities = this.anchor.getNearby(this.distance);
        entities.removeIf(e -> this.blacklist.contains(e.getUniqueId()));
        if (getContext().getCoreEntity() != null) {
            entities.remove(getContext().getCoreEntity());
        }
        List<IOrigin> filtered = filter(this.anchor, Math.max(0d, this.distance - 2), this.distance,
                this.angle, new ArrayList<>(entities));

        // put the entity on the blacklist
        for (IOrigin origin : filtered) {
            this.blacklist.add(((CoreEntity) origin).getUniqueId());
        }
        // invoke impact logic on relevant targets
        if (!filtered.isEmpty()) {
            this.impact.doMechanic(getContext(), filtered);
        }
        // render a cone of blast elements
        Location anchor = this.anchor.getLocation().clone();
        double distance = this.distance;
        Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
            // generate a ring to indicate our blast
            List<IOrigin> positions = new ArrayList<>();
            int samples = (int) ((Math.PI * distance * distance * 0.5d) / Math.sqrt(distance));
            for (int i = 0; i <= samples; i++) {
                Location position = anchor.clone();
                position.setPitch(0f);
                position.setYaw(360f * ((0f + i) / (0f + samples)));
                position.add(position.getDirection().multiply(distance));
                positions.add(new IOrigin.SnapshotOrigin(position));
            }
            // filter to a cone-like shape
            positions = filter(this.anchor, 0d, distance + 1d, angle, positions);
            // render the effects at the relevant positions
            for (IOrigin position : positions) {
                String effect_id = this.effects.get(ThreadLocalRandom.current().nextInt(this.effects.size()));
                CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(effect_id);
                effect.show(position.getLocation());
            }
        });
        // terminate if we ran out of duration
        return this.cycle > this.duration;
    }

    @Override
    public void pleaseCancelThis() {
        this.terminate = true;
    }
}
