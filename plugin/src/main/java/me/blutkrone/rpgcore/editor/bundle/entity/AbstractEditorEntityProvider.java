package me.blutkrone.rpgcore.editor.bundle.entity;

import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;

public abstract class AbstractEditorEntityProvider implements IEditorBundle {

    public abstract EntityProvider build();
}
