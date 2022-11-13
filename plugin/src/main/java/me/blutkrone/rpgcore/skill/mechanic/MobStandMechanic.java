package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import org.bukkit.Bukkit;

import java.util.List;

public class MobStandMechanic extends AbstractCoreMechanic {

    public MobStandMechanic(IEditorBundle editor) {
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        CoreEntity entity = context.getCoreEntity();
        if (entity instanceof CoreMob) {
            ((CoreMob) entity).getBase().stopWalk();
        }
    }
}
