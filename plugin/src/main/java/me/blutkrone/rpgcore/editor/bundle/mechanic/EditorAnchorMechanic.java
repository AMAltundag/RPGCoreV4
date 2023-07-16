package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.other.EditorItemModel;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.AnchorMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorAnchorMechanic extends AbstractEditorMechanic {

    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Anchor is removed after ticks have passed.")
    public EditorModifierNumber duration = new EditorModifierNumber();
    @EditorCategory(info = "Logic", icon = Material.ENDER_CHEST)
    @EditorBundle(name = "Ticker")
    @EditorTooltip(tooltip = "While anchor is active, this is ticked.")
    public EditorLogicMultiMechanic ticker = new EditorLogicMultiMechanic();
    @EditorCategory(info = "Visual", icon = Material.REDSTONE)
    @EditorBundle(name = "Model")
    @EditorTooltip(tooltip = "Item based model at the proxy position.")
    public EditorItemModel item = new EditorItemModel();

    @Override
    public AbstractCoreMechanic build() {
        return new AnchorMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ANVIL)
                .name("§fAnchor Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Anchor";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Anchor Mechanic");
        instruction.add("Runs the ticker every tick until the duration expires, the");
        instruction.add("Location is the exact position we spawned at.");
        return instruction;
    }
}
