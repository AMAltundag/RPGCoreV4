package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorLookingSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LookingSelector extends AbstractCoreSelector {

    private static final float[] CORRECTION_ANGLES = new float[]{ 5f, 10f, 20f, 30f, 50f, 80f };

    private final CoreModifierNumber distance;
    private final CoreModifierBoolean surface;

    public LookingSelector(EditorLookingSelector editor) {
        this.distance = editor.distance.build();
        this.surface = editor.surface.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        List<IOrigin> output = new ArrayList<>();

        double distance = this.distance.evalAsDouble(context);
        boolean surface = this.surface.evaluate(context);

        for (IOrigin where : previous) {
            // identify ray origin
            Location origin;
            if (where instanceof CoreEntity) {
                LivingEntity source = ((CoreEntity) where).getEntity();
                if (source == null) {
                    continue;
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
                continue;
            }
            // identify what surface to map towards
            if (!surface) {
                // map based on the direction we picked
                Location position = trace.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
                position.add(trace.getHitBlockFace().getDirection().multiply(0.5d));
                output.add(new IOrigin.SnapshotOrigin(position));
            } else if (trace.getHitBlockFace() == BlockFace.UP){
                // top is exposed, map to that
                Location position = trace.getHitBlock().getLocation().add(0.5, 1.0d, 0.5);
                output.add(new IOrigin.SnapshotOrigin(position));
            } else if (trace.getHitBlock().getRelative(BlockFace.UP).isPassable()) {
                // map to top, but check if top is exposed
                Location position = trace.getHitBlock().getLocation().add(0.5, 1.0d, 0.5);
                output.add(new IOrigin.SnapshotOrigin(position));
            }
        }

        return output;
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
}
