package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorRageSelector;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class RageSelector extends AbstractCoreSelector {

    public RageSelector(EditorRageSelector bundle) {
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
        }
        return output;
    }
}
