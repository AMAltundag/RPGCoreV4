package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorWardMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.resource.EntityWard;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.List;

public class WardMechanic extends AbstractCoreMechanic {

    private final String id;
    private final CoreModifierNumber duration;
    private final CoreModifierNumber maximum_flat;
    private final CoreModifierNumber maximum_health;
    private final CoreModifierNumber maximum_stamina;
    private final CoreModifierNumber maximum_mana;
    private final CoreModifierNumber effectiveness;
    private final CoreModifierNumber restoration;
    private final String icon;

    public WardMechanic(EditorWardMechanic editor) {
        this.id = editor.id;
        this.duration = editor.duration.build();
        this.maximum_flat = editor.maximum_flat.build();
        this.maximum_health = editor.maximum_health.build();
        this.maximum_mana = editor.maximum_mana.build();
        this.maximum_stamina = editor.maximum_stamina.build();
        this.effectiveness = editor.effectiveness.build();
        this.restoration = editor.restoration.build();
        this.icon = editor.icon;
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        double maximum_flat = this.maximum_flat.evalAsDouble(context);
        double maximum_health = this.maximum_health.evalAsDouble(context);
        double maximum_stamina = this.maximum_stamina.evalAsDouble(context);
        double maximum_mana = this.maximum_mana.evalAsDouble(context);
        double effectiveness = this.effectiveness.evalAsDouble(context);
        int restoration = this.restoration.evalAsInt(context);
        if (restoration < 0) {
            restoration = Integer.MAX_VALUE;
        }

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                CoreEntity casted = (CoreEntity) target;

                // prepare the ward to be acquired
                EntityWard ward = new EntityWard(this.icon, duration);
                ward.maximum_amount += maximum_flat;
                ward.maximum_amount += casted.getHealth().getSnapshotMaximum() * maximum_health;
                ward.maximum_amount += casted.getStamina().getSnapshotMaximum() * maximum_stamina;
                ward.maximum_amount += casted.getMana().getSnapshotMaximum() * maximum_mana;
                ward.effectiveness = effectiveness;
                ward.restoration_delay = restoration;
                // put restoration on initial cooldown & max out
                ward.restore();

                // adds new ward or replaces existing one
                casted.addEffect(this.id, ward);
            }
        }
    }
}
