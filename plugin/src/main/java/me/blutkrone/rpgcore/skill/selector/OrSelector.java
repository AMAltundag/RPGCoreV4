package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorOrSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrSelector extends AbstractCoreSelector {
    private List<AbstractCoreSelector> conditions;

    public OrSelector(EditorOrSelector editor) {
        this.conditions = AbstractEditorSelector.unwrap(editor.conditions);
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        for (AbstractCoreSelector condition : this.conditions) {
            if (!condition.doSelect(context, previous).isEmpty()) {
                return Collections.singletonList(context.getOrigin());
            }
        }

        return new ArrayList<>();
    }
}
