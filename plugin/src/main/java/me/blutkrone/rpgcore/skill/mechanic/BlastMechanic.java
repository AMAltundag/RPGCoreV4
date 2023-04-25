package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorBlastMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.BlastProxy;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Affects region ahead of us in a cone shape.
 */
public class BlastMechanic extends AbstractCoreMechanic {

    private CoreModifierNumber duration;
    private CoreModifierNumber angle;
    private CoreModifierNumber start;
    private CoreModifierNumber shrink;
    // private CoreModifierNumber up;
    private CoreModifierNumber expansion_per_second;
    private MultiMechanic impact;
    private List<String> effects;

    public BlastMechanic(EditorBlastMechanic editor) {
        this.duration = editor.duration.build();
        this.angle = editor.angle.build();
        this.start = editor.start.build();
        this.shrink = editor.shrink.build();
        // this.up = editor.up.build();
        this.expansion_per_second = editor.expansion_per_second.build();
        this.impact = editor.impact.build();
        this.effects = new ArrayList<>(editor.effects);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        double start = this.start.evalAsDouble(context);
        double expansion_per_second = this.expansion_per_second.evalAsDouble(context);
        double shrink_per_second = this.shrink.evalAsDouble(context);
        int angle = this.angle.evalAsInt(context);

        for (IOrigin target : targets) {
            IOrigin where;
            if (target instanceof CoreEntity) {
                LivingEntity entity = ((CoreEntity) target).getEntity();
                where = new IOrigin.SnapshotOrigin(entity.getEyeLocation());
            } else {
                where = target.isolate();
            }
            BlastProxy proxy = new BlastProxy(context, where, this.impact, this.effects, duration, start, expansion_per_second, angle, shrink_per_second);
            context.addProxy(proxy);
        }
    }
}
