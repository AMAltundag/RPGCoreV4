package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorInterruptMechanic;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

import java.util.List;

public class InterruptMechanic extends AbstractCoreMechanic {

    public InterruptMechanic(EditorInterruptMechanic editor) {
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        for (IOrigin target : targets) {
            if (target instanceof CorePlayer) {
                ((CorePlayer) target).interruptActivity();
            }
        }
    }
}
