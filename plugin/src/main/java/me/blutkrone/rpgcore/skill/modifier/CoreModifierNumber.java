package me.blutkrone.rpgcore.skill.modifier;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.*;

public class CoreModifierNumber {

    // numeric values
    private double base_number;
    private List<String> base_attribute;
    private double multi_number;
    private List<String> multi_attribute;
    // condition to apply modifier
    private List<AbstractCoreSelector> condition;
    // sub-modifiers that can contribute
    private List<CoreModifierNumber> children;

    public CoreModifierNumber(double number) {
        this.base_number = number;
    }

    public CoreModifierNumber(EditorModifierNumber editor) {
        this.base_number = editor.base_value;
        this.base_attribute = new ArrayList<>(editor.base_attribute);
        this.multi_number = editor.multi_value;
        this.multi_attribute = new ArrayList<>(editor.multi_attribute);
        this.condition = AbstractEditorSelector.unwrap(editor.condition);
        this.children = new ArrayList<>();
        for (IEditorBundle bundle : editor.children) {
            if (bundle instanceof EditorModifierNumber) {
                this.children.add(((EditorModifierNumber) bundle).build());
            }
        }
    }

    public int evalAsInt(IContext context) {
        return (int) evalAsDouble(context);
    }

    public double evalAsDouble(IContext context) {
        double base = 0d;
        double multi = 1d;

        Queue<CoreModifierNumber> modifiers = new LinkedList<>();
        modifiers.add(this);
        while (!modifiers.isEmpty()) {
            CoreModifierNumber remove = modifiers.poll();
            // ensure modifier meets the usage condition
            if (!AbstractCoreSelector.doSelect(this.condition, context, Collections.singletonList(context.getCoreEntity())).isEmpty()) {
                // acquire parameters of this modifier
                base += remove.base_number;
                for (String attribute : remove.base_attribute) {
                    base += context.evaluateAttribute(attribute);
                }
                multi += remove.multi_number;
                for (String attribute : remove.multi_attribute) {
                    multi += context.evaluateAttribute(attribute);
                }
                // query up child modifiers
                modifiers.addAll(remove.children);
            }
        }

        return base * Math.max(0d, multi);
    }
}
