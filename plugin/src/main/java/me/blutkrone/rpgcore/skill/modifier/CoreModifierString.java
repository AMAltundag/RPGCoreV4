package me.blutkrone.rpgcore.skill.modifier;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;

public class CoreModifierString {

    private static boolean STRING_MODIFIER_WARNING = true;

    private String string;

    public CoreModifierString(EditorModifierString editor) {
        this.string = editor.base_value;

        if (STRING_MODIFIER_WARNING) {
            RPGCore.inst().getLogger().info("not implemented (more complex string modifiers)");
            STRING_MODIFIER_WARNING = false;
        }
    }

    public CoreModifierString(String string) {
        this.string = string;
    }

    public String evaluate(IContext context) {
        return string;
    }
}
