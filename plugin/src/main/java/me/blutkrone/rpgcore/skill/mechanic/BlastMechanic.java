package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorBlastMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.BlastProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Affects region ahead of us in a cone shape.
 */
public class BlastMechanic extends AbstractCoreMechanic {

    private CoreModifierNumber duration;
    private CoreModifierNumber angle;
    private CoreModifierNumber start;
    private CoreModifierNumber up;
    private CoreModifierNumber expansion_per_second;
    private MultiMechanic impact;
    private List<String> effects;

    public BlastMechanic(EditorBlastMechanic editor) {
        this.duration = editor.duration.build();
        this.angle = editor.angle.build();
        this.start = editor.start.build();
        this.up = editor.up.build();
        this.expansion_per_second = editor.expansion_per_second.build();
        this.impact = editor.impact.build();
        this.effects = new ArrayList<>(editor.effects);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        double start = this.start.evalAsDouble(context);
        double expansion_per_second = this.expansion_per_second.evalAsDouble(context);
        int angle = this.angle.evalAsInt(context);
        double up = this.up.evalAsDouble(context);

        for (IOrigin target : targets) {
            target = target.isolate();
            target.getLocation().add(0d, up, 0d);
            BlastProxy proxy = new BlastProxy(context, target, this.impact, this.effects, duration, start, expansion_per_second, angle);
            context.getCoreEntity().getProxies().add(proxy);
        }
    }
}
