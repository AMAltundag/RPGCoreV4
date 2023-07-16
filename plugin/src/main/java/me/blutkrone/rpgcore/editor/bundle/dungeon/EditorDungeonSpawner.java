package me.blutkrone.rpgcore.editor.bundle.dungeon;

import me.blutkrone.rpgcore.dungeon.structure.AbstractDungeonStructure;
import me.blutkrone.rpgcore.dungeon.structure.SpawnerStructure;
import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.constraint.reference.index.MobConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonSpawner extends AbstractEditorDungeonStructure {

    @EditorCategory(icon = Material.SPAWNER, info = "Spawner")
    @EditorList(name = "Mobs", constraint = MobConstraint.class)
    @EditorTooltip(tooltip = {"What mobs can be spawned here"})
    public List<String> mobs = new ArrayList<>();
    @EditorNumber(name = "Level", minimum = 0)
    @EditorTooltip(tooltip = {"The level of the mob that will spawn."})
    public double level = 1d;
    @EditorNumber(name = "Leash", minimum = 0)
    @EditorTooltip(tooltip = {"Leash range, measured in chunks.", "Leash extends by 50% while in combat.", "Leash of 0 to deactivate."})
    public double leash = 0d;
    @EditorNumber(name = "Count", minimum = 1)
    @EditorTooltip(tooltip = {"How many mobs will be spawned here."})
    public double count = 1d;
    @EditorNumber(name = "Concurrent", minimum = 1, maximum = 8)
    @EditorTooltip(tooltip = {"How many mobs to spawn at once."})
    public double concurrent = 1d;
    @EditorNumber(name = "Cooldown", minimum = 0)
    @EditorTooltip(tooltip = {"Cooldown after a bunch of monsters."})
    public double cooldown = 100;
    @EditorNumber(name = "Destroy", minimum = 0, maximum = 128)
    @EditorTooltip(tooltip = {"De-spawn mob, relative to spawn-point"})
    public double despawn = 24d;

    @Override
    public AbstractDungeonStructure build() {
        return new SpawnerStructure(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.WITHER_SKELETON_SPAWN_EGG)
                .name("Spawner Structure")
                .appendLore("§cID: " + this.sync_id)
                .appendLore("§fActivation: X" + this.activation.size())
                .appendLore("§fRange: " + this.range)
                .appendLore("§fHidden: " + this.hidden)
                .appendLore("§fMobs: " + (mobs.size() == 1 ? mobs.get(0) : ("X" + mobs.size())))
                .appendLore("§fLevel: " + this.level)
                .appendLore("§fLeash: " + this.leash)
                .appendLore("§fCount: " + this.count)
                .build();
    }

    @Override
    public String getName() {
        return "Spawner";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Spawner Structure");
        instruction.add("Spawns mobs, if flagged as important will respawn");
        instruction.add("If mob de-spawned instead of dying.");
        instruction.add("");
        instruction.add("Will only spawn mobs once");
        instruction.add("If important will fix count to 1");
        return instruction;
    }
}
