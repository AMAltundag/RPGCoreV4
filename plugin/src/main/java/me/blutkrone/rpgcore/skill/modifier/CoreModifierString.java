package me.blutkrone.rpgcore.skill.modifier;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierString;
import org.bukkit.Bukkit;

public class CoreModifierString {

    private String string;

    public CoreModifierString(EditorModifierString editor) {
        this.string = editor.base_value;

        Bukkit.getLogger().severe("not implemented (more complex string modifiers)");
    }

    public CoreModifierString(String string) {
        this.string = string;
    }

    public String evaluate(IContext context) {
        return string;
    }
}
