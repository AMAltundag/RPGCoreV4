package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorMobEngage;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.ArrayList;
import java.util.List;

public class MobEngageMechanic extends AbstractCoreMechanic {

    private CoreModifierNumber distance;
    private List<AbstractCoreSelector> filter;

    public MobEngageMechanic(EditorMobEngage editor) {
        this.distance = editor.distance.build();
        this.filter = AbstractEditorSelector.unwrap(editor.filter);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        // ensure we can pull rage at all
        if (!(context.getCoreEntity() instanceof CoreMob)) {
            return;
        }
        // ensure we do not have a target already
        IEntityBase base = ((CoreMob) context.getCoreEntity()).getBase();
        if (base.getRageEntity() != null) {
            return;
        }

        // grab candidates within a certain distance
        double distance = this.distance.evalAsDouble(context);
        List<CoreEntity> candidates = context.getCoreEntity().getNearby(distance);
        candidates.remove(context.getCoreEntity());

        // apply internal filtering
        candidates.removeIf(e -> {
            // respect the circular area we can hit within
            if (e.distance(context.getCoreEntity()) > distance) {
                return true;
            }
            // ensure we are not friendly
            if (e.isFriendly(context.getCoreEntity())) {
                return true;
            }
            // ensure we share a line of sight
            if (!e.hasLineOfSight(context.getCoreEntity())) {
                return true;
            }
            // we are allowed to target
            return false;
        });

        // apply internal filtering rules
        List<IOrigin> filtered = new ArrayList<>(candidates);
        for (AbstractCoreSelector selector : filter) {
            filtered = selector.doSelect(context, filtered);
        }

        // select the target which is the closest
        IOrigin picked = null;
        double closest = Double.MAX_VALUE;
        for (IOrigin candidate : filtered) {
            double dist = candidate.distance(context.getCoreEntity());
            if (dist < closest || picked == null) {
                picked = candidate;
                closest = dist;
            }
        }

        // ensure the selector gave us an entity
        if (picked instanceof CoreEntity) {
            // compute the focus we will now hold
            double focus = ((CoreEntity) picked).evaluateAttribute("RAGE_FOCUS");
            // put the rage on the given target
            base.enrage(((CoreEntity) picked).getEntity(), 1d, 1d, focus, false);
        }
    }
}
