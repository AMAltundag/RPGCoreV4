package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.AttributeConstraint;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorAttributeAndModifier implements IEditorBundle {

    @EditorWrite(name = "Attribute", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = "Which attribute to affect!")
    public String attribute = "NOTHINGNESS";
    @EditorBundle(name = "Value")
    @EditorTooltip(tooltip = "Sums with other modifiers to this attribute.")
    public EditorModifierNumber factor = new EditorModifierNumber();

    /**
     * Useful method to unwrap a list of this class into a map.
     *
     * @param bundles bundles to unwrap
     * @return attribute mapped to factor
     */
    public static Map<String, CoreModifierNumber> unwrap(List<IEditorBundle> bundles) {
        Map<String, CoreModifierNumber> unwrapped = new HashMap<>();
        for (IEditorBundle bundle : bundles) {
            if (bundle instanceof EditorAttributeAndModifier) {
                String attribute = ((EditorAttributeAndModifier) bundle).attribute;
                unwrapped.put(attribute, ((EditorAttributeAndModifier) bundle).factor.build());
            }
        }
        return unwrapped;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aAttribute And Modifier")
                .appendLore("§fAttribute: " + attribute)
                .build();
    }

    @Override
    public String getName() {
        return "Attribute And Modifier";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Attribute And Modifier");
        instruction.add("Sum up with any other modifier to the attribute.");
        return instruction;
    }
}
