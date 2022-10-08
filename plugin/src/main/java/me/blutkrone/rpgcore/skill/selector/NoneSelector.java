package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorNoneSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoneSelector extends AbstractCoreSelector {

    public NoneSelector(EditorNoneSelector editor) {
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        if (previous.isEmpty()) {
            return Collections.singletonList(context.getCoreEntity());
        } else {
            return new ArrayList<>();
        }
    }
}
