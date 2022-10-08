package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConeSelector extends AbstractCoreSelector {

    private CoreModifierNumber radius_min;
    private CoreModifierNumber radius_max;
    private CoreModifierNumber angle;
    private List<AbstractCoreSelector> origin;

    public ConeSelector(IEditorBundle editor) {
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        double radius_min = this.radius_min.evalAsDouble(context);
        double radius_max = this.radius_max.evalAsDouble(context);
        double angle = this.angle.evalAsDouble(context);
        // identify the anchor to base the cone off
        List<IOrigin> origin = Arrays.asList(context.getCoreEntity());
        for (AbstractCoreSelector selector : this.origin) {
            origin = selector.doSelect(context, origin);
        }
        // no origin means filter failed
        if (origin.isEmpty()) {
            return previous;
        }
        // the pivot we are operating
        IOrigin pivot = origin.iterator().next();
        // filter the entities to the filtered entities
        return ConeSelector.filter(pivot, radius_min, radius_max, angle, previous);
    }

    /**
     * Applies a filter on the given subset of entities, the output
     * given is the
     *
     * @param pivot the pivot to expand the cone from
     * @param radius_min the radius of the cone
     * @param radius_max the radius of the cone
     * @param angle the angle of the cone
     * @param targets the entities to filter
     * @return the targets within the cone shape
     */
    public static List<IOrigin> filter(IOrigin pivot, double radius_min, double radius_max, double angle, List<IOrigin> targets) {
        List<IOrigin> result = new ArrayList<>();

        // squared parameters perform better
        radius_min = radius_min * radius_min;
        radius_max = radius_max * radius_max;
        // turn pivot into vectors instead
        Vector source = pivot.getLocation().toVector();
        Vector direction = pivot.getLocation().getDirection();

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
}
