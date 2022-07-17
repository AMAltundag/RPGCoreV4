package me.blutkrone.rpgcore.hud.editor.root.node;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
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

    @EditorNumber(name = "Radius", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"Radius which the node can trigger at."})
    public double radius = 16;

    public transient File file;

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
            RPGCore.inst().getGson().toJson(this, fw);
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
        instruction.add("Spawns a set of mobs from the node.");
        return instruction;
    }
}
