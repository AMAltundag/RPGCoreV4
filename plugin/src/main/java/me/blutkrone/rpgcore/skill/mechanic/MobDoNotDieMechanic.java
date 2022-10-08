package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorDoNotDieMechanic;

import java.util.List;

public class MobDoNotDieMechanic extends AbstractCoreMechanic {

    private boolean enable;

    public MobDoNotDieMechanic(EditorDoNotDieMechanic editor) {
        this.enable = editor.enable;
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        for (IOrigin target : targets) {
            if (target instanceof CoreMob) {
                ((CoreMob) target).doNotDie(this.enable);
            }
        }
    }
}
