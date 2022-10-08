package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorTauntMechanic;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class TauntMechanic extends AbstractCoreMechanic {

    // amount of rage that is generated
    public CoreModifierNumber factor;
    // rage will not exceed this limit
    public CoreModifierNumber limit;
    // bypass cooldown on swapping rage targets
    public boolean quickswap;

    public TauntMechanic(EditorTauntMechanic editor) {
        this.factor = editor.factor.build();
        this.limit = editor.limit.build();
        this.quickswap = editor.quickswap;
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        LivingEntity source = context.getCoreEntity().getEntity();
        double factor = this.factor.evalAsDouble(context);
        double limit = this.limit.evalAsDouble(context);
        double focus = context.evaluateAttribute("RAGE_FOCUS");

        for (IOrigin target : targets) {
            if (target instanceof CoreMob) {
                IEntityBase base = ((CoreMob) target).getBase();
                base.enrage(source, factor, limit, focus, this.quickswap);
            }
        }
    }
}
