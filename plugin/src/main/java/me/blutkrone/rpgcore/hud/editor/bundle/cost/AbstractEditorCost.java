package me.blutkrone.rpgcore.hud.editor.bundle.cost;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.cost.AbstractCoreCost;

public abstract class AbstractEditorCost implements IEditorBundle {

    public abstract AbstractCoreCost build();
}
