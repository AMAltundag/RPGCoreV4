package me.blutkrone.rpgcore.editor.bundle.cost;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.cost.AbstractCoreCost;

public abstract class AbstractEditorCost implements IEditorBundle {

    public abstract AbstractCoreCost build();
}
