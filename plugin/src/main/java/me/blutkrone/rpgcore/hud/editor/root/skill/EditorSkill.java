package me.blutkrone.rpgcore.hud.editor.root.skill;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorSkill implements IEditorRoot<CoreSkill> {

    @EditorWrite(name = "Name", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Name of this Skill", "§cThis is a language code, NOT plaintext."})
    public String lc_name;
    @EditorWrite(name = "Item", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Itemization of this Skill", "§cThis is a language code, NOT plaintext."})
    public String lc_item;
    @EditorWrite(name = "Binding", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Hotbar symbol when bound to a slot"})
    public String binding;
    @EditorWrite(name = "Evolution", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"WIP"})
    public String evolution_type;

    public transient File file;

    public EditorSkill() {

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
    public CoreSkill build(String id) {
        return new CoreSkill(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aSkill")
                .appendLore("§fName: " + this.lc_name)
                .appendLore("§fItem: " + this.lc_item)
                .appendLore("§fBinding: " + this.binding)
                .appendLore("§fEvolution: " + this.evolution_type)
                .build();
    }

    @Override
    public String getName() {
        return "Skill";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bSkill");
        instruction.add("An ability which can be invoked by player and monster");
        instruction.add("alike, the outcome depends on the configuration.");
        return instruction;
    }
}
