package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorFlagSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;

import java.util.ArrayList;
import java.util.List;

public class FlagSelector extends AbstractCoreSelector {

    private final CoreModifierString flag;

    public FlagSelector(EditorFlagSelector editor) {
        this.flag = editor.flag.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        String flag = this.flag.evaluate(context);

        List<IOrigin> updated = new ArrayList<>();
        for (IOrigin target : previous) {
            if (target instanceof CoreEntity && ((CoreEntity) target).checkForTag(flag)) {
                updated.add(target);
            }
        }
        return updated;
    }
}
