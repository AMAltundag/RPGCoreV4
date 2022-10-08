package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorLogicMultiMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAction;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;

import java.util.ArrayList;
import java.util.List;

/**
 * A mechanic which can invoke other mechanics and selectors, the original
 * set of targets is the initial set of targets provided.
 */
public class MultiMechanic extends AbstractCoreMechanic {

    private List<CoreAction> actions = new ArrayList<>();

    public MultiMechanic(EditorLogicMultiMechanic editor) {
        for (IEditorBundle action : editor.actions) {
            if (action instanceof EditorAction) {
                this.actions.add(((EditorAction) action).build());
            }
        }
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        for (CoreAction action : getActions()) {
            context.getCoreEntity().getActions().add(action.pipeline(context, targets));
        }
    }

    /**
     * The actions backing up this mechanic
     *
     * @return
     */
    public List<CoreAction> getActions() {
        return actions;
    }
}
