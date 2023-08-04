package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorVelocityMechanic;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VelocityMechanic extends AbstractCoreMechanic {

    private List<AbstractCoreSelector> origin;
    private CoreModifierNumber horizontal;
    private CoreModifierNumber vertical;
    private CoreModifierBoolean resistible;

    public VelocityMechanic(EditorVelocityMechanic editor) {
        this.origin = AbstractEditorSelector.unwrap(editor.anchor);
        this.horizontal = editor.horizontal.build();
        this.vertical = editor.vertical.build();
        this.resistible = editor.resistible.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double horizontal = this.horizontal.evalAsDouble(context);
        double vertical = this.vertical.evalAsDouble(context);
        boolean resistible = this.resistible.evaluate(context);

        // compute our anchor
        List<IOrigin> anchors = Collections.singletonList(context.getOrigin());
        for (AbstractCoreSelector selector : origin) {
            anchors = selector.doSelect(context, anchors);
        }
        // ensure we've got any anchor
        if (anchors.isEmpty()) {
            RPGCore.inst().getLogger().severe("No anchor could be found!");
            return;
        }
        // grab a random anchor to work with
        IOrigin anchor = anchors.get(ThreadLocalRandom.current().nextInt(anchors.size()));

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                LivingEntity entity = ((CoreEntity) target).getEntity();
                // compute direction relative to anchor
                Location a = entity.getLocation();
                Location b = anchor.getLocation();
                Vector dir = b.toVector().subtract(a.toVector()).normalize();
                dir.multiply(new Vector(horizontal, vertical, horizontal));
                // affect by resistance if needed
                if (resistible) {
                    double resistance = ((CoreEntity) target).evaluateAttribute("knockback_defense");
                    if (resistance < 0d) {
                        dir.multiply(Math.sqrt(1d + (resistance * -1)));
                    } else {
                        dir.multiply(1d / (1d+resistance));
                    }
                }
                // alter velocity of entity
                entity.setVelocity(entity.getVelocity().add(dir));
            }
        }
    }
}
