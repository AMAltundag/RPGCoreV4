package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorVelocityMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.Bukkit;
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

    public VelocityMechanic(EditorVelocityMechanic editor) {
        this.origin = AbstractEditorSelector.unwrap(editor.anchor);
        this.horizontal = editor.horizontal.build();
        this.vertical = editor.vertical.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double horizontal = this.horizontal.evalAsDouble(context);
        double vertical = this.vertical.evalAsDouble(context);

        // compute our anchor
        List<IOrigin> anchors = Collections.singletonList(context.getCoreEntity());
        for (AbstractCoreSelector selector : origin) {
            anchors = selector.doSelect(context, anchors);
        }
        // ensure we've got any anchor
        if (anchors.isEmpty()) {
            Bukkit.getLogger().severe("No anchor could be found!");
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
                // alter velocity of entity
                entity.setVelocity(entity.getVelocity().add(dir));
            }
        }
    }
}
