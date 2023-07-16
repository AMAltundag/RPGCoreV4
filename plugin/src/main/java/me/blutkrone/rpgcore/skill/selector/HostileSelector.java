package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorHostileSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;

import java.util.ArrayList;
import java.util.List;

public class HostileSelector extends AbstractCoreSelector {
    public HostileSelector(EditorHostileSelector editor) {

    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        List<IOrigin> updated = new ArrayList<>();
        for (IOrigin target : previous) {
            if (target instanceof CoreEntity && ((CoreEntity) target).isHostile(context)) {
                updated.add(target);
            }
        }

        return updated;
    }
}
