package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.BlastMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorBlastMechanic extends AbstractEditorMechanic {
    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Blast is removed after ticks have passed.")
    public EditorModifierNumber duration = new EditorModifierNumber(100);
    @EditorBundle(name = "Start")
    @EditorTooltip(tooltip = "Initial distance covered by blast.")
    public EditorModifierNumber start = new EditorModifierNumber(2);
    @EditorBundle(name = "Expansion")
    @EditorTooltip(tooltip = "Distance covered per second.")
    public EditorModifierNumber expansion_per_second = new EditorModifierNumber(1);
    @EditorBundle(name = "Angle")
    @EditorTooltip(tooltip = "Blast is removed after ticks have passed.")
    public EditorModifierNumber angle = new EditorModifierNumber(30);
    @EditorBundle(name = "Up")
    @EditorTooltip(tooltip = "Displacement on Y axis")
    public EditorModifierNumber up = new EditorModifierNumber(0.75d);
    @EditorCategory(info = "Logic", icon = Material.ENDER_CHEST)
    @EditorBundle(name = "Impact")
    @EditorTooltip(tooltip = "Logic triggered on entities hit by the blast.")
    public EditorLogicMultiMechanic impact = new EditorLogicMultiMechanic();
    @EditorCategory(info = "Visual", icon = Material.REDSTONE)
    @EditorList(name = "Effects", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = "Effects scattered along expanding blast edge")
    public List<String> effects = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new BlastMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CAULDRON)
                .name("§fBlast Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Blast";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Blast Mechanic");
        instruction.add("A forward expanding cone-line shape, only hits entities");
        instruction.add("Within the expanding edge.");
        return instruction;
    }
}
