package me.blutkrone.rpgcore.hud.editor.bundle.selector;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEditorSelector implements IEditorBundle {

    public abstract AbstractCoreSelector build();

    public static List<AbstractCoreSelector> unwrap(List<IEditorBundle> bundles) {
        List<AbstractCoreSelector> output = new ArrayList<>();
        for (IEditorBundle bundle : bundles) {
            AbstractEditorSelector casted = (AbstractEditorSelector) bundle;
            output.add(casted.build());
        }
        return output;
    }
}
