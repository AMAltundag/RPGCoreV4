package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorWalkMechanic;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WalkMechanic extends AbstractCoreMechanic {

    private List<AbstractCoreSelector> selector;
    private CoreModifierNumber speed;

    public WalkMechanic(EditorWalkMechanic editor) {
        this.selector = AbstractEditorSelector.unwrap(editor.selectors);
        this.speed = editor.speed.build();

        RPGCore.inst().getLogger().info("not implemented (do not re-path unnecessarily)");
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double speed = this.speed.evalAsDouble(context);

        // ensure we've got a mob
        CoreEntity entity = context.getCoreEntity();
        if (!(entity instanceof CoreMob)) {
            return;
        }

        // ensure we've got a selection of targets
        List<IOrigin> where = Collections.singletonList(context.getCoreEntity());
        for (AbstractCoreSelector selector : this.selector) {
            where = selector.doSelect(context, where);
        }
        if (where.isEmpty()) {
            return;
        }

        // grab a random target to walk that isn't too close
        IOrigin target = where.get(ThreadLocalRandom.current().nextInt(where.size()));
        if (target.distance(context.getCoreEntity()) <= 0.75d) {
            return;
        }

        // walk toward that target
        if (target instanceof CoreEntity) {
            ((CoreMob) entity).getBase().walkTo(((CoreEntity) target).getEntity(), speed);
        } else {
            ((CoreMob) entity).getBase().walkTo(target.getLocation(), speed);
        }
    }
}
