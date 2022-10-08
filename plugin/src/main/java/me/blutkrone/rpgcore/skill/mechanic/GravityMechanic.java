package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorGravityMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

public class GravityMechanic extends AbstractCoreMechanic {
    public CoreModifierNumber power;

    public GravityMechanic(EditorGravityMechanic editor) {
        this.power = editor.power.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double power = this.power.evalAsDouble(context);

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                LivingEntity entity = ((CoreEntity) target).getEntity();
                entity.setVelocity(entity.getVelocity().add(new Vector(0d, power, 0d)));
            }
        }
    }
}
