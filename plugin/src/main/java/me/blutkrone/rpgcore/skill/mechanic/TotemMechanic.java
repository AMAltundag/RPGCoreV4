package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.entity.AbstractEditorEntityProvider;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorTotemMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.AbstractSkillProxy;
import me.blutkrone.rpgcore.skill.proxy.TotemProxy;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Living creature that defines position we invoke
 * a skill at, early termination possible thorough
 * death of creature.
 */
public class TotemMechanic extends AbstractCoreMechanic {

    private static final float[] CORRECTION_ANGLES = new float[]{5f, 10f, 20f, 30f, 50f, 80f};

    private final CoreModifierBoolean exact;
    private List<AbstractCoreSelector> filter;
    private CoreModifierNumber multi;
    private CoreModifierNumber limit;
    private CoreModifierNumber duration;
    private CoreModifierNumber interval;
    private CoreModifierNumber health;
    private MultiMechanic logic_on_tick;
    private MultiMechanic logic_on_finish;
    private EntityProvider provider;

    public TotemMechanic(EditorTotemMechanic editor) {
        this.exact = editor.exact.build();
        this.multi = editor.multi.build();
        this.limit = editor.limit.build();
        this.duration = editor.duration.build();
        this.interval = editor.interval.build();
        this.health = editor.health.build();
        this.logic_on_tick = editor.logic_on_tick.build();
        this.logic_on_finish = editor.logic_on_finish.build();
        this.provider = ((AbstractEditorEntityProvider) editor.factory.get(0)).build();
        this.filter = AbstractEditorSelector.unwrap(editor.filter);
    }

    /*
     * Counts the trap proxies that the entity has active.
     *
     * @param entity whose trap proxies to count
     * @return how many traps we have
     */
    private int count(CoreEntity entity) {
        int output = 0;
        List<AbstractSkillProxy> proxies = entity.getProxies();
        for (AbstractSkillProxy proxy : proxies) {
            if (proxy instanceof TotemProxy) {
                output += 1;
            }
        }
        return output;
    }

    /*
     * Following 'looking' logic, try finding a good spot for a totem.
     *
     * @param where the anchor of the totem
     * @param distance spawn distance of totem
     * @return a spawn anchor, or null
     */
    private IOrigin getWantedPosition(IOrigin where, double distance) {
        // identify ray origin
        Location origin;
        if (where instanceof CoreEntity) {
            LivingEntity source = ((CoreEntity) where).getEntity();
            if (source == null) {
                return null;
            }
            origin = source.getEyeLocation();
        } else {
            origin = where.getLocation();
        }
        // throw casts to find the hit block
        RayTraceResult trace = getHit(origin, distance);
        for (int i = 0; i < CORRECTION_ANGLES.length && (trace == null || trace.getHitBlock() == null || trace.getHitBlockFace() == null); i++) {
            Location corrected_origin = origin.clone();
            corrected_origin.setPitch(Math.min(90f, origin.getPitch() + CORRECTION_ANGLES[i]));
            trace = getHit(corrected_origin, distance);
        }
        // ensure we found something or skip
        if (trace == null || trace.getHitBlock() == null || trace.getHitBlockFace() == null) {
            return null;
        }
        // identify what surface to map towards
        if (trace.getHitBlockFace() == BlockFace.UP) {
            // top is exposed, map to that
            Location position = trace.getHitBlock().getLocation().add(0.5, 1.0d, 0.5);
            return new IOrigin.SnapshotOrigin(position);
        } else if (trace.getHitBlock().getRelative(BlockFace.UP).isPassable()) {
            // map to top, but check if top is exposed
            Location position = trace.getHitBlock().getLocation().add(0.5, 1.0d, 0.5);
            return new IOrigin.SnapshotOrigin(position);
        } else {
            // invalid spot to summon totem at
            return null;
        }
    }

    /*
     * Find the block that was hit.
     *
     * @param source the source of our ray
     * @param distance how long a ray to cast
     * @return the block we've hit
     */
    private RayTraceResult getHit(Location source, double distance) {
        World world = source.getWorld();
        if (world == null) {
            return null;
        }
        Vector direction = source.getDirection();
        RayTraceResult strike = world.rayTraceBlocks(source, direction, distance, FluidCollisionMode.NEVER, true);
        if (strike == null || strike.getHitBlock() == null) {
            return null;
        }
        return strike;
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        int interval = this.interval.evalAsInt(context);
        int health = this.health.evalAsInt(context);
        double multi = 1d + Math.max(0d, this.multi.evalAsDouble(context));
        int limit = this.limit.evalAsInt(context);
        boolean exact = this.exact.evaluate(context);
        int active = count(context.getCoreEntity());

        if (exact) {
            // spawn one totem at the exact location, respecting limit
            for (IOrigin target : targets) {
                if (active < limit) {
                    TotemProxy proxy = new TotemProxy(context, target, interval, duration, this.provider, this.logic_on_tick, this.logic_on_finish, health, this.filter);
                    context.getCoreEntity().getProxies().add(proxy);
                    active += 1;
                }
            }
        } else {
            for (IOrigin target : targets) {
                // find exact location we are facing
                IOrigin spawn_where = getWantedPosition(target, 8d);
                // cannot spawn if we are facing an illegal area
                if (spawn_where == null) {
                    continue;
                }
                // spawn one totem at where we are facing
                if (active < limit) {
                    TotemProxy proxy = new TotemProxy(context, spawn_where, interval, duration, this.provider, this.logic_on_tick, this.logic_on_finish, health, this.filter);
                    context.getCoreEntity().getProxies().add(proxy);
                    active += 1;
                    proxy.getTotem().getEntity().setVelocity(new Vector(0d, 0.2d, 0d));
                }
                // additional totems are floored at proximity
                double multi_remaining = multi - 1d;
                while (Math.random() < multi_remaining-- && active < limit) {
                    // spawn totem at the given spot
                    TotemProxy proxy = new TotemProxy(context, spawn_where, interval, duration, this.provider, this.logic_on_tick, this.logic_on_finish, health, this.filter);
                    context.getCoreEntity().getProxies().add(proxy);
                    active += 1;
                    // bounce to a proper location
                    proxy.getTotem().getEntity().setVelocity(new Vector(Math.random() - 0.5, 0.2d, Math.random() - 0.5));
                }
            }
        }
    }
}
