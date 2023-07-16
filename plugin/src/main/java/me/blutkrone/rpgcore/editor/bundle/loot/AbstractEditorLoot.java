package me.blutkrone.rpgcore.editor.bundle.loot;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;

public abstract class AbstractEditorLoot implements IEditorBundle {

    public abstract AbstractCoreLoot build();
}
