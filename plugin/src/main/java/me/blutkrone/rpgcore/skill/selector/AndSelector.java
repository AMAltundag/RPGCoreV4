package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorAndSelector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AndSelector extends AbstractCoreSelector {

    private List<AbstractCoreSelector> conditions;

    public AndSelector(EditorAndSelector editor) {
        this.conditions = AbstractEditorSelector.unwrap(editor.conditions);
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        if (!AbstractCoreSelector.doSelect(this.conditions, context, previous).isEmpty()) {
            return Collections.singletonList(context.getOrigin());
        } else {
            return new ArrayList<>();
        }
    }
}