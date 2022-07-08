package me.blutkrone.rpgcore.hud.editor.root;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.NPCConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.StringConstraint;
import me.blutkrone.rpgcore.node.impl.CoreNodeSpawnerNPC;
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
 * A node which can spawn an NPC
 */
public class EditorNodeSpawnerNPC implements IEditorRoot<CoreNodeSpawnerNPC> {

    @EditorWrite(name = "Permission", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Permission needed to engage with node.", "Use 'none' to let everyone use it."})
    public String permission = "none";
    @EditorNumber(name = "Radius", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"Radius which the node can trigger at."})
    public double radius = 16;
    @EditorWrite(name = "NPC", constraint = NPCConstraint.class)
    @EditorTooltip(tooltip = "Which NPC to spawn.")
    public String npc = "NOTHINGNESS";

    public transient File file;

    public EditorNodeSpawnerNPC() {
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGson().toJson(this, fw);
        }
    }

    @Override
    public CoreNodeSpawnerNPC build(String id) {
        return new CoreNodeSpawnerNPC(id, this);
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
        instruction.add("§bNPC Spawner Node");
        instruction.add("Spawns an NPC when players are nearby.");
        return instruction;
    }
}
