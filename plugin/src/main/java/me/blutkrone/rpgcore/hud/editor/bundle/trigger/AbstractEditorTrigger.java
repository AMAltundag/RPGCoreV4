package me.blutkrone.rpgcore.hud.editor.bundle.trigger;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.trigger.AbstractCoreTrigger;

public abstract class AbstractEditorTrigger implements IEditorBundle {

    public abstract AbstractCoreTrigger build();
}
