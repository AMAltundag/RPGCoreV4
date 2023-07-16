package me.blutkrone.rpgcore.editor.root.passive;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.PassiveConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.passive.CorePassiveNode;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorPassiveNode implements IEditorRoot<CorePassiveNode> {

    // @EditorList(name = "Parts", constraint = EffectPartConstraint.class)
    // public List<IEditorBundle> effects = new ArrayList<>();

    @EditorWrite(name = "Allocated", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Item used if node is assigned.", "§cThis is a language code, NOT plaintext."})
    public String lc_allocated = "NOTHINGNESS";
    @EditorWrite(name = "Unallocated", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Item used if node is NOT assigned.", "§cThis is a language code, NOT plaintext."})
    public String lc_unallocated = "NOTHINGNESS";
    @EditorList(name = "Effects", constraint = PassiveConstraint.class, singleton = true)
    @EditorTooltip(tooltip = "The effect on the entity while the node is allocated.")
    public List<IEditorBundle> effects = new ArrayList<>();

    public transient File file;
    public int migration_version = RPGCore.inst().getMigrationManager().getVersion();

    public EditorPassiveNode() {
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
    public CorePassiveNode build(String id) {
        return new CorePassiveNode(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.END_CRYSTAL)
                .name("§aPassive Node")
                .build();
    }

    @Override
    public String getName() {
        return "Node";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Passive Node");
        instruction.add("A passive node grants access to certain");
        instruction.add("Effects while allocated, multiple of the");
        instruction.add("Same node are able to stack.");
        return instruction;
    }
}
