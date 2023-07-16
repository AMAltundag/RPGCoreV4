package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorStrollMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.List;

public class StrollMechanic extends AbstractCoreMechanic {

    private CoreModifierNumber minimum;
    private CoreModifierNumber maximum;
    private CoreModifierNumber speed;

    public StrollMechanic(EditorStrollMechanic editor) {
        this.minimum = editor.minimum.build();
        this.maximum = editor.maximum.build();
        this.speed = editor.speed.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int minimum = this.minimum.evalAsInt(context);
        int maximum = this.maximum.evalAsInt(context);
        double speed = this.speed.evalAsDouble(context);

        CoreEntity entity = context.getCoreEntity();
        // cannot stroll unless a mob
        if (!(entity instanceof CoreMob)) {
            return;
        }

        // cannot stroll while already walking
        if (((CoreMob) entity).getBase().isWalking()) {
            return;
        }

        // cannot stroll while holding rage
        if (((CoreMob) entity).getBase().getRageEntity() != null) {
            return;
        }

        // stroll randomly within a valid distance
        ((CoreMob) entity).getBase().stroll(minimum, maximum, speed, (((CoreMob) entity)::isValidStrollTarget));
    }
}
