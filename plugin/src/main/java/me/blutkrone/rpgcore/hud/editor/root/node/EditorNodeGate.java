package me.blutkrone.rpgcore.hud.editor.root.node;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.node.impl.CoreNodeGate;
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
 * A node used as an entrance for dungeon content
 */
public class EditorNodeGate implements IEditorRoot<CoreNodeGate> {

    @EditorList(name = "Content", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"What content is accessible from this gate"})
    public List<String> content = new ArrayList<>();
    @EditorNumber(name = "Radius", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"Players will see a menu if within range"})
    public double radius = 6d;

    public transient File file;

    public EditorNodeGate() {
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
    public CoreNodeGate build(String id) {
        return new CoreNodeGate(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aGate Node")
                .build();
    }

    @Override
    public String getName() {
        return "Gate";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bGate Node");
        instruction.add("Entrance into dungeon content, if not in a group this");
        instruction.add("Matchmaking will be performed.");
        instruction.add("");
        instruction.add("If only one piece of content is listed, it'll directly");
        instruction.add("Pick that one for the player.");
        instruction.add("");
        instruction.add("Bungee server will override player limit for matchmaking.");
        return instruction;
    }
}
