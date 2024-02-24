package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.ModelSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorModelSelector extends AbstractEditorSelector {

    @EditorBundle(name = "Bone")
    @EditorTooltip(tooltip = {"ID of the bone to select"})
    public EditorModifierString bone = new EditorModifierString("head");
    @EditorBundle(name = "Normalize")
    @EditorTooltip(tooltip = {"Enabled: Rotation is relative to parent bone", "Disabled: Rotation is relative to entity rotation"})
    public EditorModifierBoolean normalized = new EditorModifierBoolean(false);

    @Override
    public AbstractCoreSelector build() {
        return new ModelSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BUCKET)
                .name("§fModel Selector")
                .appendLore("§fBone: " + this.bone.base_value)
                .build();
    }

    @Override
    public String getName() {
        return "Model Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Model Selector");
        instruction.add("Select a bone from the model, will map to the entity");
        instruction.add("If we arent a modeled entity or the bone doesn't exist.");
        return instruction;
    }
}
