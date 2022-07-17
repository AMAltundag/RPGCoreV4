package me.blutkrone.rpgcore.hud.editor.root.npc;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.TraitConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.ItemConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorNPC implements IEditorRoot<CoreNPC> {

    @EditorCategory(icon = Material.CRAFTING_TABLE, info = "General")
    @EditorWrite(name = "Name", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Name to use for the NPC", "§cThis is a language code, NOT plaintext."})
    public String lc_name = "NOTHINGNESS";
    @EditorWrite(name = "Skin", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"MineSkin URL", "Paste full URL here!"})
    public String skin = "NOTHINGNESS";
    @EditorList(name = "Traits", constraint = TraitConstraint.class)
    @EditorTooltip(tooltip = {"Traits allow interacting with NPC entities."})
    public List<IEditorBundle> traits = new ArrayList<>();

    @EditorCategory(icon = Material.BOOKSHELF, info = "Miscellaneous")
    @EditorBoolean(name = "Staring")
    @EditorTooltip(tooltip = {"Stares at the player"})
    public boolean staring = false;

    @EditorCategory(icon = Material.IRON_CHESTPLATE, info = "Equipment")
    @EditorWrite(name = "Helmet", constraint = ItemConstraint.class)
    public String item_helmet = "NOTHINGNESS";
    @EditorWrite(name = "Chestplate", constraint = ItemConstraint.class)
    public String item_chestplate = "NOTHINGNESS";
    @EditorWrite(name = "Pants", constraint = ItemConstraint.class)
    public String item_pants = "NOTHINGNESS";
    @EditorWrite(name = "Boots", constraint = ItemConstraint.class)
    public String item_boots = "NOTHINGNESS";
    @EditorWrite(name = "Mainhand", constraint = ItemConstraint.class)
    public String item_mainhand = "NOTHINGNESS";
    @EditorWrite(name = "Offhand", constraint = ItemConstraint.class)
    public String item_offhand = "NOTHINGNESS";

    private transient File file;

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
    public CoreNPC build(String id) {
        return new CoreNPC(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aNPC")
                .appendLore("§fName: " + RPGCore.inst().getLanguageManager().getTranslation(this.lc_name))
                .appendLore("§fSkin: " + this.skin)
                .build();
    }

    @Override
    public String getName() {
        return RPGCore.inst().getLanguageManager().getTranslation(this.lc_name);
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fNPC");
        instruction.add("§fEntity meant for miscellaneous purposes. Use for Quests,");
        instruction.add("§fVendors, Quests, Background. Not meant for combat.");
        return instruction;
    }
}