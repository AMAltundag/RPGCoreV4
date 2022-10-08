package me.blutkrone.rpgcore.skill.modifier;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierBoolean;

import java.util.HashSet;
import java.util.Set;

public class CoreModifierBoolean {

    private boolean defaults;
    private Set<String> enable = new HashSet<>();
    private Set<String> disable = new HashSet<>();

    public CoreModifierBoolean(boolean defaults) {
        this.defaults = defaults;
    }

    public CoreModifierBoolean(EditorModifierBoolean editor) {
        this.defaults = editor.default_value;
        this.enable.addAll(editor.enable);
        this.disable.addAll(editor.disable);
    }

    public boolean evaluate(IContext context) {
        // tags that may disable this
        for (String tag : this.disable) {
            if (context.checkForTag(tag)) {
                return false;
            }
        }
        // tags that may enable this
        for (String tag : this.enable) {
            if (context.checkForTag(tag)) {
                return true;
            }
        }
        // default is disabled
        return this.defaults;
    }
}
