package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorLogicMultiMechanic;
import me.blutkrone.rpgcore.editor.bundle.other.EditorAction;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;

import java.util.ArrayList;
import java.util.List;

/**
 * A mechanic which can invoke other mechanics and selectors, the original
 * set of targets is the initial set of targets provided.
 */
public class MultiMechanic extends AbstractCoreMechanic {

    public List<CoreAction> actions = new ArrayList<>();

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
            CoreAction.ActionPipeline pipeline = action.pipeline(context, targets);
            if (!pipeline.update()) {
                context.addPipeline(pipeline);
            }
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
