package me.blutkrone.rpgcore.editor.root.job;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.constraint.reference.index.JobConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.TreeConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.job.CoreJob;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorJob implements IEditorRoot<CoreJob> {

    @EditorBoolean(name = "Defaults")
    @EditorTooltip(tooltip = {"Offer this job during character creation?"})
    public boolean defaults;
    @EditorWrite(name = "Emblem", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Itemized emblem representing the job", "§cThis is a language code, NOT plaintext."})
    public String lc_emblem = "NOTHINGNESS";
    @EditorWrite(name = "Weapon", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Itemized weapon representing the job", "§cThis is a language code, NOT plaintext."})
    public String lc_weapon = "NOTHINGNESS";
    @EditorList(name = "Trees", constraint = TreeConstraint.class)
    @EditorTooltip(tooltip = {"All passive trees available with this job", "Will apply the passive tree from here", "§cNever grant access of tree from two sources!"})
    public List<String> passive_tree = new ArrayList<>();
    @EditorList(name = "Advance", constraint = JobConstraint.class)
    @EditorTooltip(tooltip = "Advancements offered when advancement condition is met")
    public List<String> advanced_jobs = new ArrayList<>();

    public transient File file;
    public int migration_version = RPGCore.inst().getMigrationManager().getVersion();

    public EditorJob() {

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
    public CoreJob build(String id) {
        return new CoreJob(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aJob")
                .appendLore("§fDefaults: " + this.defaults)
                .appendLore("§fEmblem: " + this.lc_emblem)
                .appendLore("§fWeapon: " + this.lc_weapon)
                .build();
    }

    @Override
    public String getName() {
        return "Job";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bJob");
        instruction.add("A job grants additional power scaling to a player,");
        instruction.add("thorough a passive tree and dedicated abilities.");
        instruction.add("");
        instruction.add("§cNever grant access to the same tree from two sources!");
        return instruction;
    }
}
