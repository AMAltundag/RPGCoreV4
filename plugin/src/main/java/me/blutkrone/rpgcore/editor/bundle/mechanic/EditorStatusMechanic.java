package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.AttributeAndModifierConstraint;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.AttributeConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.StatusMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorStatusMechanic extends AbstractEditorMechanic {

    @EditorWrite(name = "ID", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Same ID will instead gain stacks.")
    public String id = UUID.randomUUID().toString();
    @EditorList(name = "Attribute", constraint = AttributeAndModifierConstraint.class)
    @EditorTooltip(tooltip = "Attributes while holding effect")
    public List<IEditorBundle> attribute = new ArrayList<>();
    @EditorList(name = "Scaling", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = "Multiplies all attributes (base is 1.0)")
    public List<String> scaling = new ArrayList<>();
    @EditorBoolean(name = "Debuff")
    @EditorTooltip(tooltip = "This affects only the icon rendering")
    public boolean debuff = false;
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Duration of the effect")
    public EditorModifierNumber duration = new EditorModifierNumber();
    @EditorBundle(name = "Stack")
    @EditorTooltip(tooltip = "Limit on how often this stacks")
    public EditorModifierNumber stack = new EditorModifierNumber();
    @EditorWrite(name = "Icon", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Icon shown on the HUD")
    public String icon = "none";

    @Override
    public AbstractCoreMechanic build() {
        return new StatusMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.LINGERING_POTION)
                .name("§fStatus")
                .appendLore("§fID: " + id)
                .appendLore("§fAttributes: " + attribute.size())
                .appendLore("§fScaling: " + scaling.size())
                .appendLore("§fDebuff: " + debuff)
                .appendLore("§fIcon: " + icon)
                .build();
    }

    @Override
    public String getName() {
        return "Status";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Status Mechanic");
        instruction.add("An effect which will grant attributes to the recipient, should");
        instruction.add("we have the effect already you gain a stack and refresh to the");
        instruction.add("duration.");
        instruction.add("");
        instruction.add("Attributes are snapshot from the first effect.");
        return instruction;
    }
}
