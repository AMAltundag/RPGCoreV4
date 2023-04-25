package me.blutkrone.rpgcore.hud.editor.bundle.dungeon;

import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.dungeon.structure.CheckpointStructure;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonCheckpoint extends AbstractEditorDungeonStructure {

    @Override
    public AbstractDungeonStructure build() {
        return new CheckpointStructure(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.RESPAWN_ANCHOR)
                .name("§fCheckpoint Structure")
                .appendLore("§cID: " + this.sync_id)
                .appendLore("§fActivation: X" + this.activation.size())
                .appendLore("§fRange: " + this.range)
                .appendLore("§fHidden: " + this.hidden)
                .build();
    }

    @Override
    public String getName() {
        return "Checkpoint";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Checkpoint Structure");
        instruction.add("Players will respawn at checkpoint if they die");
        instruction.add("");
        instruction.add("Only applies to deaths in dungeon");
        return instruction;
    }
}
