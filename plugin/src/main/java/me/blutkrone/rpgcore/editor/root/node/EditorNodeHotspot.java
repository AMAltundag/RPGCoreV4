package me.blutkrone.rpgcore.editor.root.node;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.node.impl.CoreNodeHotspot;
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
 * A hotspot is mainly used for quests to track special
 * locations.
 */
public class EditorNodeHotspot implements IEditorRoot<CoreNodeHotspot> {

    @EditorCategory(icon = Material.COMPASS, info = "Hotspot")
    @EditorNumber(name = "Radius", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"Radius which the node can trigger at."})
    public double radius = 16;

    public transient File file;
    public int migration_version = RPGCore.inst().getMigrationManager().getVersion();

    public EditorNodeHotspot() {
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
    public CoreNodeHotspot build(String id) {
        return new CoreNodeHotspot(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.MAP)
                .name("§aHotspot Node")
                .build();
    }

    @Override
    public String getName() {
        return "Hotspot";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bHotspot Node");
        instruction.add("Used by quests to mark locations");
        return instruction;
    }
}
