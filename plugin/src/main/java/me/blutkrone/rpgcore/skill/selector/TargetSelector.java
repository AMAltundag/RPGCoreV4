package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorTargetSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class TargetSelector extends AbstractCoreSelector {

    public TargetSelector(EditorTargetSelector bundle) {
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        List<IOrigin> output = new ArrayList<>();
        CoreEntity entity = context.getCoreEntity();
        if (entity instanceof CoreMob) {
            LivingEntity target = ((CoreMob) entity).getBase().getRageEntity();
            if (target != null) {
                CoreEntity core_target = RPGCore.inst().getEntityManager().getEntity(target);
                if (core_target != null) {
                    output.add(core_target);
                }
            }
        } else if (entity instanceof CorePlayer) {
            CoreEntity focused = ((CorePlayer) entity).getFocusTracker().getFocus();
            if (focused != null) {
                output.add(focused);
            }
        }

        return output;
    }
}
