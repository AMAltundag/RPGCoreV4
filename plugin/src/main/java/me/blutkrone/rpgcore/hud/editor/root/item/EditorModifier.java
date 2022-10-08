package me.blutkrone.rpgcore.hud.editor.root.item;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorHideWhen;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorName;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAttributeAndFactor;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.AttributeAndFactorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.ModifierStyleConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.ModifierTypeConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.item.modifier.CoreModifier;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;
import me.blutkrone.rpgcore.item.modifier.ModifierType;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EditorName(name = "Modifier")
@EditorTooltip(tooltip = "A modifier to influence the entity")
public class EditorModifier implements IEditorRoot<CoreModifier> {

    // identifies the modifier for targeted operations
    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"A modifier can be identified by the tags", "Only 'AFFIX' will roll the modifier randomly."})
    public List<String> tags = new ArrayList<>();

    // base weight when applying random operations on modifier
    @EditorNumber(name = "Weight")
    @EditorTooltip(tooltip = "Chance to be rolled, compared to other affixes.")
    @EditorHideWhen(field = "implicit", value = "true")
    public double weight = 0.0;
    // multiplies with quality and adds to weight
    @EditorNumber(name = "Quality")
    @EditorTooltip(tooltip = "Weight gained based off the quality.")
    @EditorHideWhen(field = "implicit", value = "true")
    public double weight_per_quality = 0.0;

    // which attributes are gained while equipped
    @EditorList(name = "Effects", constraint = AttributeAndFactorConstraint.class)
    @EditorTooltip(tooltip = "Attributes gained when item is in use.")
    public List<EditorAttributeAndFactor> attribute_effects = new ArrayList<>();
    // how to apply the modifier to the relevant entity
    @EditorWrite(name = "Type", constraint = ModifierTypeConstraint.class)
    @EditorTooltip(tooltip = "What scope the attributes apply on.")
    public ModifierType type = ModifierType.ENTITY;
    // readable information to present
    @EditorWrite(name = "Readable", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Description of modifier, usage depends on style.", "§cThis is a language code, NOT plaintext."})
    public String lc_readable = "NOTHINGNESS";
    // how to present the style
    @EditorWrite(name = "Style", constraint = ModifierStyleConstraint.class)
    @EditorTooltip(tooltip = {"Readable Style", "How to present the readable information."})
    public ModifierStyle readable_style = ModifierStyle.GENERIC;
    // which category to file under, does not work with a header
    @EditorWrite(name = "Category", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Organize non-header type descriptions.", "§cThis is a language code, NOT plaintext."})
    public String lc_category = "NOTHINGNESS";

    private boolean implicit = false;
    private transient File file = null;

    public EditorModifier() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aModifier")
                .appendLore("§fTags: " + tags.stream().collect(Collectors.joining(", ")))
                .appendLore("§fWeight: " + String.format("%.3f", weight))
                .appendLore("§fWeight Per Quality: " + String.format("%.3f", weight_per_quality))
                .appendLore("§fTotal Modifiers: " + attribute_effects.size())
                .build();
    }

    @Override
    public String getName() {
        return "Modifier";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Modifier");
        instruction.add("A modifier which affects the entity when the item is used");
        return instruction;
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
    public CoreModifier build(String id) {
        return new CoreModifier(id, this);
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit() {
        this.implicit = true;
    }
}
