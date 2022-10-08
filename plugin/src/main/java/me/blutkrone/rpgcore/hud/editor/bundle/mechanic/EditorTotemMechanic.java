package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.EntityProviderConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.TotemMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorTotemMechanic extends AbstractEditorMechanic {
    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Totem is removed after ticks have passed.")
    public EditorModifierNumber duration = new EditorModifierNumber();
    @EditorBundle(name = "Ticked")
    @EditorTooltip(tooltip = "Interval of how often the totem is ticked.")
    public EditorModifierNumber interval = new EditorModifierNumber();
    @EditorBundle(name = "Health")
    @EditorTooltip(tooltip = "Health the totem is spawned with.")
    public EditorModifierNumber health = new EditorModifierNumber();
    @EditorCategory(info = "Logic", icon = Material.ENDER_CHEST)
    @EditorBundle(name = "Ticked")
    @EditorTooltip(tooltip = "Logic triggered while totem is alive.")
    public EditorLogicMultiMechanic logic_on_tick = new EditorLogicMultiMechanic();
    @EditorBundle(name = "Finished")
    @EditorTooltip(tooltip = "Logic triggered before totem is removed.")
    public EditorLogicMultiMechanic logic_on_finish = new EditorLogicMultiMechanic();
    @EditorCategory(info = "Visual", icon = Material.REDSTONE)
    @EditorList(name = "Entity", singleton = true, constraint = EntityProviderConstraint.class)
    @EditorTooltip(tooltip = {"An entity spawned to represent the totem"})
    public List<IEditorBundle> factory = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new TotemMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.WITHER_SKELETON_SKULL)
                .name("§fTotem Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Totem";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Totem Mechanic");
        instruction.add("Totem will be removed when taking too much damage, skills");
        instruction.add("Are cast by the summoner at the location of the totem. The");
        instruction.add("Totem itself does not use a skill.");
        return instruction;
    }
}
