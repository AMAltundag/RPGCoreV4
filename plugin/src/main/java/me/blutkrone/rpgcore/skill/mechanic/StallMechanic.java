package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorMobStallMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.ArrayList;
import java.util.List;

public class StallMechanic extends AbstractCoreMechanic {

    private List<AbstractCoreSelector> conditions = new ArrayList<>();

    public StallMechanic(EditorMobStallMechanic editor) {
        for (IEditorBundle condition : editor.condition) {
            this.conditions.add(((AbstractEditorSelector) condition).build());
        }
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        throw new UnsupportedOperationException("Cannot be invoked directly!");
     }

    /**
     * The mob is allowed to continue with their AI logic, once they've
     * fulfilled the condition.
     *
     * @return condition is fulfilled if we pass the mob thorough every
     *         selector and have anyone remaining at the end of it.
     */
    public List<AbstractCoreSelector> getCondition() {
        return conditions;
    }
}
