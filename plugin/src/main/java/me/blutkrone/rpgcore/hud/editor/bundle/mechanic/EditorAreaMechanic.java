package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorItemModel;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.AreaMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorAreaMechanic extends AbstractEditorMechanic {

    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Area is removed after ticks have passed.")
    public EditorModifierNumber duration = new EditorModifierNumber();
    @EditorBundle(name = "Inner")
    @EditorTooltip(tooltip = "Inner radius of the area.")
    public EditorModifierNumber inner_radius = new EditorModifierNumber();
    @EditorBundle(name = "Outer")
    @EditorTooltip(tooltip = "Outer radius of the area.")
    public EditorModifierNumber outer_radius = new EditorModifierNumber();
    @EditorCategory(info = "Logic", icon = Material.ENDER_CHEST)
    @EditorBundle(name = "Cooldown")
    @EditorTooltip(tooltip = "Cooldown before same entity is targeted.")
    public EditorModifierNumber cooldown = new EditorModifierNumber();
    @EditorBundle(name = "Ticker")
    @EditorTooltip(tooltip = "Logic triggered while the area is active.")
    public EditorLogicMultiMechanic ticker = new EditorLogicMultiMechanic();
    @EditorBundle(name = "Impact")
    @EditorTooltip(tooltip = "Logic triggered on entities within radius.")
    public EditorLogicMultiMechanic impact = new EditorLogicMultiMechanic();
    @EditorList(name = "Filter", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Additional filter to restrict impacted entities.")
    public List<IEditorBundle> filter = new ArrayList<>();
    @EditorCategory(info = "Visual", icon = Material.REDSTONE)
    @EditorBundle(name = "Model")
    @EditorTooltip(tooltip = {"Item based model at the proxy center."})
    public EditorItemModel item = new EditorItemModel();
    @EditorList(name = "Effects", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = "Effects spawned at center of the area")
    public List<String> effects = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new AreaMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CAULDRON)
                .name("§fArea Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Area";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Area Mechanic");
        instruction.add("Affects all entities within a radius by the impact, and");
        instruction.add("Applies the ticker on the center of the area.");
        return instruction;
    }
}
