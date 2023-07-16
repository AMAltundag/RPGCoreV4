package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorSelfSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelfSelector extends AbstractCoreSelector {

    public SelfSelector(EditorSelfSelector editor) {
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        CoreEntity entity = context.getCoreEntity();
        if (entity != null) {
            return Collections.singletonList(entity);
        } else {
            return new ArrayList<>();
        }
    }
}
