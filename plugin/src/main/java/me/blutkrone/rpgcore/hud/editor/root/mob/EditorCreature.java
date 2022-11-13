package me.blutkrone.rpgcore.hud.editor.root.mob;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.AttributeAndFactorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.LogicConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.EntityProviderConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.LootConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.mob.CoreCreature;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorCreature implements IEditorRoot<CoreCreature> {

    @EditorCategory(info = "Basics", icon = Material.CHEST)
    @EditorList(name = "Type", singleton = true, constraint = EntityProviderConstraint.class)
    @EditorTooltip(tooltip = "Defines the way the entity is put into the world.")
    public List<IEditorBundle> factory = new ArrayList<>();
    @EditorList(name = "Loot", constraint = LootConstraint.class)
    @EditorTooltip(tooltip = "Loot offered to killer upon death.")
    public List<IEditorBundle> loot = new ArrayList<>();
    @EditorWrite(name = "Sigil", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Texture shown on the left of a focus bar.")
    public String focus_sigil = "default";
    @EditorWrite(name = "Name", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = "Renders the name in appropriate places")
    public String lc_name = "NOTHINGNESS";

    @EditorCategory(info = "Attributes", icon = Material.EXPERIENCE_BOTTLE)
    @EditorList(name = "Base", constraint = AttributeAndFactorConstraint.class)
    @EditorTooltip(tooltip = "Given to all instances of the creature.")
    public List<IEditorBundle> attributes_base = new ArrayList<>();
    @EditorList(name = "Level", constraint = AttributeAndFactorConstraint.class)
    @EditorTooltip(tooltip = "Multiplied with level before adding to creature.")
    public List<IEditorBundle> attributes_level = new ArrayList<>();

    @EditorCategory(info = "Logic", icon = Material.COMPARATOR)
    @EditorList(name = "Spawned", constraint = LogicConstraint.class)
    @EditorTooltip(tooltip = {"Invoked upon spawning the creature.", "Limited to 1 (highest priority!)"})
    public List<IEditorBundle> ai_mono = new ArrayList<>();
    @EditorList(name = "Ticked", constraint = LogicConstraint.class)
    @EditorTooltip(tooltip = "One logic for each group is run at a time, every tick.")
    public List<IEditorBundle> ai_tick = new ArrayList<>();
    @EditorList(name = "Death", constraint = LogicConstraint.class)
    @EditorTooltip(tooltip = {"Invoked prior to death.", "Entity will die unconditionally afterwards!", "Limited to 1 (highest priority!)"})
    public List<IEditorBundle> ai_death = new ArrayList<>();

    @EditorCategory(info = "Relations", icon = Material.BLUE_BANNER)
    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Tags that identify this entity"})
    public List<String> tags = new ArrayList<>();
    @EditorList(name = "Friendly", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Hostile to entities that have any of these tags"})
    public List<String> friendly_tag = new ArrayList<>();
    @EditorList(name = "Hostile", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Friendly to entities that have any of these tags"})
    public List<String> hostile_tag = new ArrayList<>();

    public transient File file;

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
    public CoreCreature build(String id) {
        return new CoreCreature(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.WITHER_SKELETON_SPAWN_EGG)
                .build();
    }

    @Override
    public String getName() {
        return "Creature";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Creature");
        instruction.add("Template for mobs spawned into the world.");
        instruction.add("");
        instruction.add("Players have the 'PLAYER' tag, others are configured.");
        instruction.add("Friendly supersedes hostile check.");
        instruction.add("If summoned, relationship is checked via parent.");

        return instruction;
    }
}