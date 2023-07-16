package me.blutkrone.rpgcore.editor.bundle.trigger;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.skill.trigger.AbstractCoreTrigger;

public abstract class AbstractEditorTrigger implements IEditorBundle {

    public abstract AbstractCoreTrigger build();
}
