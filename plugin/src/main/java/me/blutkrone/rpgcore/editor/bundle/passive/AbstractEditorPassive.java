package me.blutkrone.rpgcore.editor.bundle.passive;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.passive.node.AbstractCorePassive;

public abstract class AbstractEditorPassive implements IEditorBundle {

    public abstract AbstractCorePassive build();

}
