package me.blutkrone.rpgcore.hud.editor.bundle.passive;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.passive.node.AbstractCorePassive;

public abstract class AbstractEditorPassive implements IEditorBundle {

    public abstract AbstractCorePassive build();

}
