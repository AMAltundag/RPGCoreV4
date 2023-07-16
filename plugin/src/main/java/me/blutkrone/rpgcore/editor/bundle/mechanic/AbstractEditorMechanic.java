package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;

public abstract class AbstractEditorMechanic implements IEditorBundle {

    public abstract AbstractCoreMechanic build();
}
