package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorHideWhen;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.other.EditorItemModel;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.BeamMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorBeamMechanic extends AbstractEditorMechanic {

    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Beam is removed after ticks have passed.")
    public EditorModifierNumber duration = new EditorModifierNumber();
    @EditorBundle(name = "Range")
    @EditorTooltip(tooltip = {"Initial range of the beam"})
    public EditorModifierNumber range_original = new EditorModifierNumber();
    @EditorBundle(name = "Expansion")
    @EditorTooltip(tooltip = {"Distance gained per second"})
    public EditorModifierNumber range_per_second = new EditorModifierNumber();
    @EditorBundle(name = "Maximum")
    @EditorTooltip(tooltip = {"Upper limit on expansion distance"})
    public EditorModifierNumber range_maximum = new EditorModifierNumber();
    @EditorBundle(name = "Freestyle")
    @EditorTooltip(tooltip = {"Face any direction, but cannot rotate."})
    public EditorModifierBoolean freestyle = new EditorModifierBoolean();
    @EditorBundle(name = "Offset")
    @EditorHideWhen(field = "freestyle", value = "true")
    @EditorTooltip(tooltip = {"Initial offset of rotation, in degrees."})
    public EditorModifierNumber rotation_offset = new EditorModifierNumber();
    @EditorBundle(name = "Rotation")
    @EditorHideWhen(field = "freestyle", value = "true")
    @EditorTooltip(tooltip = {"Rotation per second, in degrees."})
    public EditorModifierNumber rotation_per_second = new EditorModifierNumber();
    @EditorCategory(info = "Logic", icon = Material.ENDER_CHEST)
    @EditorBundle(name = "Cooldown")
    @EditorTooltip(tooltip = "Cooldown before same entity is targeted.")
    public EditorModifierNumber cooldown = new EditorModifierNumber();
    @EditorBundle(name = "Impact")
    @EditorTooltip(tooltip = "Logic triggered on entities hit by beam.")
    public EditorLogicMultiMechanic impact = new EditorLogicMultiMechanic();
    @EditorList(name = "Filter", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Additional filter to restrict impacted entities.")
    public List<IEditorBundle> filter = new ArrayList<>();
    @EditorCategory(info = "Visual", icon = Material.REDSTONE)
    @EditorBundle(name = "Model")
    @EditorTooltip(tooltip = {"Item based model at the proxy center."})
    public EditorItemModel item = new EditorItemModel();
    @EditorList(name = "Beam", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = "Effects scattered along beam length")
    public List<String> beam_effects = new ArrayList<>();
    @EditorList(name = "Head", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = "Effects at start and finish position")
    public List<String> head_effects = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new BeamMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CAULDRON)
                .name("§fBeam Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Beam";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Beam Mechanic");
        instruction.add("A freestyle beam can be aimed at any XYZ axis, otherwise");
        instruction.add("you can give it a rotation that spins on the XZ axis. The");
        instruction.add("Beam cannot pierce entities or blocks.");
        return instruction;
    }
}
