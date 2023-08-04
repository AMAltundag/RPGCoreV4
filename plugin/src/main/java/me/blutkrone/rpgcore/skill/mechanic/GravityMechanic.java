package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorGravityMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

public class GravityMechanic extends AbstractCoreMechanic {
    private CoreModifierNumber power;
    private CoreModifierBoolean resistible;

    public GravityMechanic(EditorGravityMechanic editor) {
        this.power = editor.power.build();
        this.resistible = editor.resistible.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double power = this.power.evalAsDouble(context);
        boolean resistible = this.resistible.evaluate(context);

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                LivingEntity entity = ((CoreEntity) target).getEntity();
                Vector dir = new Vector(0d, power, 0d);
                if (resistible) {
                    double resistance = ((CoreEntity) target).evaluateAttribute("knockback_defense");
                    if (resistance < 0d) {
                        dir.multiply(Math.sqrt(1d + (resistance * -1)));
                    } else {
                        dir.multiply(1d / (1d+resistance));
                    }
                }
                entity.setVelocity(entity.getVelocity().add(dir));
            }
        }
    }
}
