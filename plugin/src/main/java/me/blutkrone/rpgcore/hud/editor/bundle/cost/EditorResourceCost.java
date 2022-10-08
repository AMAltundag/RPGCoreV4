package me.blutkrone.rpgcore.hud.editor.bundle.cost;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.cost.AbstractCoreCost;
import me.blutkrone.rpgcore.skill.cost.ResourceCost;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorResourceCost extends AbstractEditorCost {

    @EditorBundle(name = "Cost")
    @EditorTooltip(tooltip = "How much is needed for this cost.")
    public EditorModifierNumber cost = new EditorModifierNumber(0d);
    @EditorBundle(name = "Stamina")
    @EditorTooltip(tooltip = "Draws cost from stamina instead of mana.")
    public EditorModifierBoolean stamina = new EditorModifierBoolean();
    @EditorBundle(name = "Blood")
    @EditorTooltip(tooltip = "Highest priority, draws cost from life.")
    public EditorModifierBoolean blood = new EditorModifierBoolean();

    @Override
    public AbstractCoreCost build() {
        return new ResourceCost(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BREWING_STAND)
                .name("Resource Cost")
                .build();
    }

    @Override
    public String getName() {
        return "Resource";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Resource Cost");
        instruction.add("Consume a certain amount of a resource, blood will");
        instruction.add("Override stamina (i.E mana -> stamina -> health)");
        return instruction;
    }
}
