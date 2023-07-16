package me.blutkrone.rpgcore.editor.root.job;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.StringAndNumberConstraint;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.job.CoreProfession;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorProfession implements IEditorRoot<CoreProfession> {

    @EditorList(name = "Level", constraint = StringAndNumberConstraint.class)
    @EditorTooltip(tooltip = {"Tags required to level up", "Quest tags are prefixed with 'quest_'"})
    public List<IEditorBundle> level_requirement = new ArrayList<>();

    public transient File file;
    public int migration_version = RPGCore.inst().getMigrationManager().getVersion();

    public EditorProfession() {
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
    public CoreProfession build(String id) {
        return new CoreProfession(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aProfession")
                .build();
    }

    @Override
    public String getName() {
        return "Profession";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bProfession");
        instruction.add("Progression for crafting, collecting etc.");
        instruction.add("");
        instruction.add("§cNever grant access to the same tree from two sources!");
        return instruction;
    }
}
