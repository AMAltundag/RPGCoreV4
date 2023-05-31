package me.blutkrone.rpgcore.hud.editor.bundle.dungeon;

import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.dungeon.structure.TreasureStructure;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.TreasureConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonTreasure extends AbstractEditorDungeonStructure {

    @EditorList(name = "Treasures", constraint = TreasureConstraint.class)
    @EditorTooltip(tooltip = {"Treasures which can spawn", "Only computed once when this is activated"})
    public List<IEditorBundle> treasures = new ArrayList<>();

    @Override
    public AbstractDungeonStructure build() {
        return new TreasureStructure(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("Treasure Structure")
                .appendLore("§cID: " + this.sync_id)
                .appendLore("§fActivation: X" + this.activation.size())
                .appendLore("§fRange: " + this.range)
                .appendLore("§fHidden: " + this.hidden)
                .appendLore("§fTreasure: X" + this.treasures.size())
                .build();
    }

    @Override
    public String getName() {
        return "Treasure";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Treasure Structure");
        instruction.add("Treasure chest which holds items, these hold personal loot");
        instruction.add("For every player.");
        instruction.add("");
        instruction.add("If you do not meet any condition, nothing will spawn.");
        return instruction;
    }
}