package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorWalkMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
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
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double speed = this.speed.evalAsDouble(context);

        List<IOrigin> where = Collections.singletonList(context.getCoreEntity());
        for (AbstractCoreSelector selector : this.selector) {
            where = selector.doSelect(context, where);
        }

        if (!where.isEmpty()) {
            // approach target if distance is fine
            IOrigin target = where.get(ThreadLocalRandom.current().nextInt(where.size()));
            if (target.distance(context.getCoreEntity()) > 0.75d) {
                CoreEntity entity = context.getCoreEntity();
                if (entity instanceof CoreMob) {
                    ((CoreMob) entity).getBase().walkTo(target.getLocation(), speed);
                }
            }
        }
    }
}
