package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;

public abstract class AbstractEditorMechanic implements IEditorBundle {

    public abstract AbstractCoreMechanic build();
}
