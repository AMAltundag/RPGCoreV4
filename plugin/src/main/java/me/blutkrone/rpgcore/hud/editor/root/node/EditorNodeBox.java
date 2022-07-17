package me.blutkrone.rpgcore.hud.editor.root.node;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.LootConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.MaterialConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.node.impl.CoreNodeBox;
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
public class EditorNodeBox implements IEditorRoot<CoreNodeBox> {

    @EditorNumber(name = "Radius", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"Radius which the node can trigger at."})
    public double radius = 16;
    @EditorList(name = "Weight", constraint = LootConstraint.class)
    @EditorTooltip(tooltip = "Weight multipliers to roll with.")
    public List<IEditorBundle> item_weight = new ArrayList<>();
    @EditorNumber(name = "Total", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"How many slots will spawn with an item."})
    public double item_total = 4;
    @EditorNumber(name = "Cooldown", minimum = 0)
    @EditorTooltip(tooltip = {"Time to recover for another usage."})
    public double cooldown = 2;
    @EditorNumber(name = "Size", minimum = 1, maximum = 127)
    @EditorTooltip(tooltip = {"Size of collider box."})
    public double collide_size = 3;

    @EditorCategory(icon = Material.GREEN_WOOL, info = "Available Model")
    @EditorWrite(name = "Material", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = {"Icon used while node is available."})
    public Material available_icon = Material.STONE_PICKAXE;
    @EditorNumber(name = "Model")
    @EditorTooltip(tooltip = {"Model used while node is available"})
    public double available_model = 0;
    @EditorCategory(icon = Material.GRAY_WOOL, info = "Unavailable Model")
    @EditorWrite(name = "Material", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = {"Icon used while node is not available."})
    public Material unavailable_icon = Material.STONE_PICKAXE;
    @EditorNumber(name = "Model", minimum = 0)
    @EditorTooltip(tooltip = {"Model used while node is not available"})
    public double unavailable_model = 0;

    public transient File file;

    public EditorNodeBox() {
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
    public CoreNodeBox build(String id) {
        return new CoreNodeBox(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aBox Node")
                .appendLore("§fTotal: " + this.item_total)
                .appendLore("§fWeights: X" + this.item_weight.size())
                .build();
    }

    @Override
    public String getName() {
        return "Box";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bSpawner Box");
        instruction.add("Spawns a box with randomized items.");
        return instruction;
    }
}
