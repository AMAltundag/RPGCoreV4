package me.blutkrone.rpgcore.hud.editor.root.skill;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorSkillInfo;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.BehaviourConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.SkillInfoConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SkillBindingConstraint;
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
    public String lc_name = "NOTHINGNESS";
    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Allows to identify skills by certain keywords"})
    public List<String> tags = new ArrayList<>();
    @EditorBoolean(name = "Hidden")
    @EditorTooltip(tooltip = {"Hide from player until an external source grants the skill"})
    public boolean hidden = false;
    @EditorBoolean(name = "Passive")
    @EditorTooltip(tooltip = {"While skill is not hidden, all passives are acquired", "If disabled, passives are granted if skill on skillbar"})
    public boolean passive = false;

    @EditorList(name = "Binding", singleton = true, constraint = SkillBindingConstraint.class)
    @EditorTooltip(tooltip = "What type of binding to make skill accessible")
    public List<IEditorBundle> skill_binding = new ArrayList<>();
    @EditorList(name = "Passive", constraint = BehaviourConstraint.class)
    @EditorTooltip(tooltip = {"Additional behaviours passively on the entity", "These are assigned while the skills are active"})
    public List<IEditorBundle> behaviours = new ArrayList<>();

    @EditorList(name = "Info", constraint = SkillInfoConstraint.class)
    @EditorTooltip(tooltip = {"Additional info to render on itemized skill."})
    public List<EditorSkillInfo> info_modifiers = new ArrayList<>();

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
            RPGCore.inst().getGsonPretty().toJson(this, fw);
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
