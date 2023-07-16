package me.blutkrone.rpgcore.item;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.other.EditorRequirement;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.Collections;
import java.util.List;

public class Requirement {

    private List<AbstractCoreSelector> conditions;
    private String display;

    public Requirement(EditorRequirement editor) {
        display = editor.display;
        conditions = AbstractEditorSelector.unwrap(editor.conditions);
    }

    /**
     * The hint to provide if condition is met.
     *
     * @return Hint to show
     */
    public List<String> getDisplayText() {
        return RPGCore.inst().getLanguageManager().getTranslationList(this.display);
    }

    /**
     * Check if this requirement is met.
     *
     * @param entity Who to check against.
     * @return Conditions are met?
     */
    public boolean doesArchive(CoreEntity entity) {
        return AbstractCoreSelector.doSelect(this.conditions, entity, Collections.singletonList(entity)).size() > 0;
    }
}
