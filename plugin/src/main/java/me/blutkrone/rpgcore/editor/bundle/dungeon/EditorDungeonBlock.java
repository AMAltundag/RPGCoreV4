package me.blutkrone.rpgcore.editor.bundle.dungeon;

import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.dungeon.structure.BlockStructure;
import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.constraint.enums.MaterialConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonBlock extends AbstractEditorDungeonStructure {

    @EditorCategory(icon = Material.COMPARATOR, info = "Block")
    @EditorBoolean(name = "Permanent")
    @EditorTooltip(tooltip = "Will only invoke once when activation is fulfilled")
    public boolean permanent = false;
    @EditorNumber(name = "Interval")
    @EditorTooltip(tooltip = "Interval to check activation condition.")
    public double interval = 20d;
    @EditorWrite(name = "Success", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = "Material used if activation is fulfilled.")
    public Material material_success = Material.REDSTONE_BLOCK;
    @EditorWrite(name = "Failure", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = "Material used if activation isn't fulfilled.")
    public Material material_failure = Material.AIR;
    @EditorBoolean(name = "Physics")
    @EditorTooltip(tooltip = "Apply physics when updating")
    public boolean physics = false;
    @EditorNumber(name = "Proliferate", minimum = 0)
    @EditorTooltip(tooltip = {
            "Spread the changes to identical adjacent blocks.",
            "Must save/load the world to update proliferation.",
            "",
            "§cPerforms better then many small block structures"
    })
    public double proliferate = 0;

    // cache of proliferation counted from the source
    public List<Proliferation> proliferations = new ArrayList<>();

    @Override
    public AbstractDungeonStructure build() {
        return new BlockStructure(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.COMPARATOR)
                .name("Block Structure")
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
        return "Block";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Block Structure");
        instruction.add("Apply a block based on a condition, do note that the");
        instruction.add("Update only happens if a player is within range.");
        instruction.add("Use with caution, limit decorative use.");
        instruction.add("");
        instruction.add("§cBlock operations have great performance impact!");
        return instruction;
    }

    public static class Proliferation {
        public final BlockVector source;
        public final List<BlockVector> targets;

        public Proliferation(BlockVector source, List<BlockVector> targets) {
            this.source = source;
            this.targets = targets;
        }
    }
}
