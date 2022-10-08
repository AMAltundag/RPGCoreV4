package me.blutkrone.rpgcore.hud.editor.bundle.loot;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;

public abstract class AbstractEditorLoot implements IEditorBundle {

    public abstract AbstractCoreLoot build();
}
