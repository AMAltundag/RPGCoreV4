package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorThrustMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

public class ThrustMechanic extends AbstractCoreMechanic {
    private CoreModifierNumber power;
    private CoreModifierNumber drag;
    private CoreModifierBoolean resistible;

    public ThrustMechanic(EditorThrustMechanic editor) {
        this.power = editor.power.build();
        this.drag = editor.drag.build();
        this.resistible = editor.resistible.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double power = this.power.evalAsDouble(context);
        double drag = this.drag.evalAsDouble(context);
        boolean resistible = this.resistible.evaluate(context);

        if (drag >= 0.01d) {
            drag = Math.sqrt(1d + Math.max(0d, drag));
            drag = 1d - (1d / drag);
        }
        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                LivingEntity entity = ((CoreEntity) target).getEntity();
                Vector before = entity.getVelocity();
                if (drag <= 0.01d) {
                    // adds to velocity
                    Vector after = entity.getEyeLocation().getDirection();

                    Vector dir = after.multiply(power);
                    if (resistible) {
                        double resistance = ((CoreEntity) target).evaluateAttribute("knockback_defense");
                        if (resistance < 0d) {
                            dir.multiply(Math.sqrt(1d + (resistance * -1)));
                        } else {
                            dir.multiply(1d / (1d+resistance));
                        }
                    }

                    entity.setVelocity(before.add(dir));
                } else {
                    // sets as velocity
                    Vector after = entity.getEyeLocation().getDirection();
                    Vector merge = before.multiply(drag).add(after.multiply(1d - drag));

                    Vector dir = merge.normalize().multiply(power);
                    if (resistible) {
                        double resistance = ((CoreEntity) target).evaluateAttribute("knockback_defense");
                        if (resistance < 0d) {
                            dir.multiply(Math.sqrt(1d + (resistance * -1)));
                        } else {
                            dir.multiply(1d / (1d+resistance));
                        }
                    }

                    entity.setVelocity(dir);
                }
            }
        }
    }
}
