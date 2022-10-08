package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.ChainMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorChainMechanic extends AbstractEditorMechanic {
    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Chains")
    @EditorTooltip(tooltip = "Total entities we can chain off.")
    public EditorModifierNumber chains = new EditorModifierNumber();
    @EditorBundle(name = "Delay")
    @EditorTooltip(tooltip = "Delay before we will chain again.")
    public EditorModifierNumber delay = new EditorModifierNumber();
    @EditorBundle(name = "Speed")
    @EditorTooltip(tooltip = "Radius we are chaining within.")
    public EditorModifierNumber radius = new EditorModifierNumber();
    @EditorCategory(info = "Logic", icon = Material.ENDER_CHEST)
    @EditorBundle(name = "Impact")
    @EditorTooltip(tooltip = "Logic triggered on entities we chained to.")
    public EditorLogicMultiMechanic impact = new EditorLogicMultiMechanic();
    @EditorList(name = "Filter", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Additional filter to restrict chain targets.")
    public List<IEditorBundle> filter = new ArrayList<>();
    @EditorCategory(info = "Visual", icon = Material.REDSTONE)
    @EditorList(name = "Effects", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = "Effects spawned on target after chaining")
    public List<String> effects = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new ChainMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.WITHER_SKELETON_SKULL)
                .name("§fChain Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Chain";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Chain Mechanic");
        instruction.add("Instantly moves between entities within a radius, only if");
        instruction.add("there is a line-of-sight between those entities.");
        return instruction;
    }
}
