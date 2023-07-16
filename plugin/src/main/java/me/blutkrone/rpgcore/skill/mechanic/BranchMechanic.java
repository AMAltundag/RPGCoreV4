package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorLogicBranchMechanic;
import me.blutkrone.rpgcore.editor.bundle.other.EditorBranch;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * A mechanic that can contain multiple branches of logic.
 */
public class BranchMechanic extends AbstractCoreMechanic {

    private List<Branch> branches = new ArrayList<>();

    public BranchMechanic(EditorLogicBranchMechanic editor) {
        for (IEditorBundle branch : editor.branches) {
            if (branch instanceof EditorBranch) {
                branches.add(new Branch((EditorBranch) branch));
            }
        }
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        throw new UnsupportedOperationException("Cannot be invoked directly!");
    }

    /**
     * Retrieve the mechanic from the branch which can be invoked.
     *
     * @param context the context to invoke within.
     * @return the mechanic to invoke or null
     */
    public MultiMechanic getMechanic(IContext context) {
        for (Branch branch : branches) {
            // check if we've got enough targets clustered
            List<IOrigin> condition = new ArrayList<>();
            for (AbstractCoreSelector selector : branch.condition) {
                condition = selector.doSelect(context, condition);
            }
            // if condition is met, run on that branch
            if (!condition.isEmpty()) {
                return branch.mechanic;
            }
        }

        return null;
    }

    private class Branch {
        private MultiMechanic mechanic;
        private List<AbstractCoreSelector> condition = new ArrayList<>();

        public Branch(EditorBranch editor) {
            this.mechanic = (MultiMechanic) editor.mechanic.build();
            for (IEditorBundle bundle : editor.condition) {
                condition.add(((AbstractEditorSelector) bundle).build());
            }
        }
    }
}