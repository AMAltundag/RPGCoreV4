package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.EntityProviderConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.TotemMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorTotemMechanic extends AbstractEditorMechanic {
    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Exact")
    @EditorTooltip(tooltip = {"Disables multi totem, spawns on location", "If not enabled, searches for direction."})
    public EditorModifierBoolean exact = new EditorModifierBoolean();
    @EditorBundle(name = "Multi")
    @EditorTooltip(tooltip = {"Summons multiple totems at once."})
    public EditorModifierNumber multi = new EditorModifierNumber(1.0d);
    @EditorBundle(name = "Limit")
    @EditorTooltip(tooltip = "Maximum totems placed, 0 means we cannot place anymore.")
    public EditorModifierNumber limit = new EditorModifierNumber(12.0d);
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
    @EditorList(name = "Filter", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"Totem can trigger, if any targets remain.", "Original target is the totem location."})
    public List<IEditorBundle> filter = new ArrayList<>();

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
        instruction.add("Create totems and cast a skill at their location.");
        instruction.add("Totem can take damage and die.");
        instruction.add("");
        instruction.add("The caster will use the skill, NOT the totem. The cast");
        instruction.add("Just happens on the location of the totem.");
        instruction.add("");
        instruction.add("The location search of non-exact follows the looking logic.");
        return instruction;
    }
}
