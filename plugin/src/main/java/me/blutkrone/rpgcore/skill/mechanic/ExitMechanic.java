package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;

import java.util.List;

public class ExitMechanic extends AbstractCoreMechanic {

    public ExitMechanic(IEditorBundle bundle) {
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        throw new UnsupportedOperationException("Cannot be invoked directly!");
    }
}
