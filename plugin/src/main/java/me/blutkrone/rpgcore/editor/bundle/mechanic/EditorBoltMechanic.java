package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.other.EditorItemModel;
import me.blutkrone.rpgcore.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.BoltMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorBoltMechanic extends AbstractEditorMechanic {
    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Lifespan of bolt")
    public EditorModifierNumber duration = new EditorModifierNumber(100);
    @EditorBundle(name = "Radius")
    @EditorTooltip(tooltip = "Size of the bolt")
    public EditorModifierNumber radius = new EditorModifierNumber(0.5d);
    @EditorBundle(name = "Pierce")
    @EditorTooltip(tooltip = "How many entities can be pierced")
    public EditorModifierNumber pierce = new EditorModifierNumber(0.0d);
    @EditorBundle(name = "Speed")
    @EditorTooltip(tooltip = "Blocks moved per tick")
    public EditorModifierNumber speed = new EditorModifierNumber(4.0d);
    @EditorCategory(info = "Logic", icon = Material.ENDER_CHEST)
    @EditorBundle(name = "Impact")
    @EditorTooltip(tooltip = "Logic triggered on entities hit by the bolt.")
    public EditorLogicMultiMechanic impact = new EditorLogicMultiMechanic();
    @EditorCategory(info = "Visual", icon = Material.REDSTONE)
    @EditorBundle(name = "Model")
    @EditorTooltip(tooltip = {"Item based model at the proxy position.", "This is updated during the movement."})
    public EditorItemModel item = new EditorItemModel();
    @EditorList(name = "Effects", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = "Effects placed at tip of the bolt projectile")
    public List<String> effects = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new BoltMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ARROW)
                .name("§fBolt Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Bolt";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Bolt Mechanic");
        instruction.add("Projectile moving in a straight line, upon impact can");
        instruction.add("Pierce thorough the entity.");
        return instruction;
    }
}
