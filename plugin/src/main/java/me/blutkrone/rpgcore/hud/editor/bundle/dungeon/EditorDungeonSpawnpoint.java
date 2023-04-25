package me.blutkrone.rpgcore.hud.editor.bundle.dungeon;

import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.dungeon.structure.SpawnpointStructure;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonSpawnpoint extends AbstractEditorDungeonStructure {

    @Override
    public AbstractDungeonStructure build() {
        return new SpawnpointStructure(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.LIME_BED)
                .name("§fSpawnpoint Structure")
                .appendLore("§cID: " + this.sync_id)
                .appendLore("§fActivation: X" + this.activation.size())
                .appendLore("§fRange: " + this.range)
                .appendLore("§fHidden: " + this.hidden)
                .build();
    }

    @Override
    public String getName() {
        return "Spawnpoint";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Spawnpoint Structure");
        instruction.add("Entrance location for players, if multiple spawnpoints");
        instruction.add("Are set will pick a random one.");
        instruction.add("");
        instruction.add("Requirements are ignored.");
        return instruction;
    }
}
