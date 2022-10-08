package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorFriendlySelector;

import java.util.ArrayList;
import java.util.List;

public class FriendlySelector extends AbstractCoreSelector {
    public FriendlySelector(EditorFriendlySelector editor) {

    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        List<IOrigin> updated = new ArrayList<>();
        for (IOrigin target : previous) {
            if (target instanceof CoreEntity && ((CoreEntity) target).isFriendly(context.getCoreEntity())) {
                updated.add(target);
            }
        }
        return updated;
    }
}
