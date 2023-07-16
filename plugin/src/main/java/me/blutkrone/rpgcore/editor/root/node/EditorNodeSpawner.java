package me.blutkrone.rpgcore.editor.root.node;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.constraint.reference.index.MobConstraint;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.node.impl.CoreNodeSpawner;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * A node refers to a specific point on the map which
 * serve as a point-of-interest to a player.
 */
public class EditorNodeSpawner implements IEditorRoot<CoreNodeSpawner> {

    @EditorCategory(icon = Material.SPAWNER, info = "Spawner")
    @EditorNumber(name = "Radius", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"Radius which the node can trigger at."})
    public double radius = 16d;
    @EditorNumber(name = "Count", minimum = 0, maximum = 8)
    @EditorTooltip(tooltip = {"How many mobs will be spawned here."})
    public double count = 1d;
    @EditorNumber(name = "Cooldown", minimum = 0)
    @EditorTooltip(tooltip = {"Cooldown counts from when all mobs die."})
    public double cooldown = 200d;
    @EditorNumber(name = "Level", minimum = 0)
    @EditorTooltip(tooltip = {"The level of the mob that will spawn."})
    public double level = 1d;
    @EditorNumber(name = "Leash", minimum = 0)
    @EditorTooltip(tooltip = {"Leash range, measured in chunks.", "Leash extends by 50% while in combat."})
    public double leash = 0d;
    @EditorList(name = "Mobs", constraint = MobConstraint.class)
    @EditorTooltip(tooltip = {"What mobs can be spawned here"})
    public List<String> mobs = new ArrayList<>();

    public transient File file;
    public int migration_version = RPGCore.inst().getMigrationManager().getVersion();

    public EditorNodeSpawner() {
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGsonPretty().toJson(this, fw);
        }
    }

    @Override
    public CoreNodeSpawner build(String id) {
        return new CoreNodeSpawner(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aSpawner Node")
                .appendLore("§fRadius: " + radius)
                .appendLore("§fCount: " + count)
                .appendLore("§fCooldown: " + cooldown)
                .appendLore("§fLevel: " + level)
                .appendLore("§fLeash: " + leash)
                .appendLore("§fMobs: " + mobs)
                .build();
    }

    @Override
    public String getName() {
        return "Spawner";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bSpawner Node");
        instruction.add("Spawns mobs, if the node has no mobs spawned and we've");
        instruction.add("Passed the cooldown. Updates every 3 seconds.");
        instruction.add("");
        instruction.add("Leash is a best effort, do not expect accuracy.");
        return instruction;
    }
}
