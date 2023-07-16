package me.blutkrone.rpgcore.skill.modifier;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;

public class CoreModifierString {

    private String string;

    public CoreModifierString(EditorModifierString editor) {
        this.string = editor.base_value;

        RPGCore.inst().getLogger().info("not implemented (more complex string modifiers)");
    }

    public CoreModifierString(String string) {
        this.string = string;
    }

    public String evaluate(IContext context) {
        return string;
    }
}
