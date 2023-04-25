package me.blutkrone.rpgcore.hud.editor.bundle.dungeon;

import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.dungeon.structure.SwapperStructure;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.MaterialConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonBlockSwapper extends AbstractEditorDungeonStructure {

    @EditorCategory(icon = Material.COMPARATOR, info = "Block")
    @EditorBoolean(name = "Permanent")
    @EditorTooltip(tooltip = "Will only invoke once when activation is fulfilled")
    public boolean permanent = false;
    @EditorNumber(name = "Interval")
    @EditorTooltip(tooltip = "Interval to check activation condition.")
    public double interval = 60d;
    @EditorWrite(name = "Success", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = "Material used if activation is fulfilled.")
    public Material material_success = Material.REDSTONE_BLOCK;
    @EditorWrite(name = "Failure", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = "Material used if activation isn't fulfilled.")
    public Material material_failure = Material.AIR;
    @EditorBoolean(name = "Physics")
    @EditorTooltip(tooltip = "Apply physics when updating")
    public boolean physics = false;

    @Override
    public AbstractDungeonStructure build() {
        return new SwapperStructure(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.COMPARATOR)
                .name("Swapper Structure")
                .appendLore("§cID: " + this.sync_id)
                .appendLore("§fActivation: X" + this.activation.size())
                .appendLore("§fRange: " + this.range)
                .appendLore("§fHidden: " + this.hidden)
                .appendLore("§fPermanent: " + this.permanent)
                .appendLore("§fInterval: " + this.interval)
                .appendLore("§fSuccessful: " + this.material_success)
                .appendLore("§fFailure: " + this.material_failure)
                .appendLore("§fPhysics: " + this.physics)
                .build();
    }

    @Override
    public String getName() {
        return "Swapper";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Swapper Structure");
        instruction.add("Swap a block if condition is being met, if you use");
        instruction.add("This to trigger redstone remember that a player must");
        instruction.add("Be within 48 blocks to update it.");
        instruction.add("");
        instruction.add("Be careful about performance impact");
        return instruction;
    }
}
