package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorFaceMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FaceMechanic extends AbstractCoreMechanic {

    private final List<AbstractCoreSelector> where;

    public FaceMechanic(EditorFaceMechanic editor) {
        where = AbstractEditorSelector.unwrap(editor.where);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        // pick the anchor point to pick
        List<IOrigin> where = Collections.singletonList(context.getOrigin());
        for (AbstractCoreSelector selector : this.where) {
            where = selector.doSelect(context, where);
        }
        // cannot do this if we have no targets
        if (where.isEmpty()) {
            return;
        }
        // pick one random location to look at
        IOrigin target = where.get(ThreadLocalRandom.current().nextInt(where.size()));
        // make entities face this exact location
        for (IOrigin entity : targets) {
            if (entity instanceof CoreEntity) {
                LivingEntity handle = ((CoreEntity) entity).getEntity();
                Vector dir = target.getLocation().clone().subtract(handle.getEyeLocation()).toVector();
                Location loc = handle.getLocation().setDirection(dir);
                handle.teleport(loc);
            }
        }

    }
}
