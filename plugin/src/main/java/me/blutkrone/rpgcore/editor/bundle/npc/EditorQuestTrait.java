package me.blutkrone.rpgcore.editor.bundle.npc;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.QuestConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreQuestTrait;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorQuestTrait extends AbstractEditorNPCTrait {

    @EditorCategory(info = "Quest", icon = Material.BOOK)
    @EditorList(name = "Quests", constraint = QuestConstraint.class)
    @EditorTooltip(tooltip = "Quests which can be started here")
    public List<String> quests = new ArrayList<>();

    @EditorCategory(info = "Cortex", icon = Material.FURNACE)
    @EditorWrite(name = "Icon", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Symbol to show on the NPC menu", "Only relevant with multiple NPC traits."})
    public String symbol = "default";
    @EditorWrite(name = "Text", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Description of this trait", "§cThis is a language code, NOT plaintext."})
    public String lc_text = "NOTHINGNESS";
    @EditorWrite(name = "Unlock", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Flag needed to show the trait"})
    public String unlock = "none";

    public transient File file;

    public EditorQuestTrait() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("§fBanker Trait")
                .build();
    }

    @Override
    public String getName() {
        return "Banker";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Banker Trait");
        return instruction;
    }

    @Override
    public AbstractCoreTrait build() {
        return new CoreQuestTrait(this);
    }

    @Override
    public String getCortexSymbol() {
        return this.symbol;
    }

    @Override
    public String getIconLC() {
        return this.lc_text;
    }

    @Override
    public String getUnlockFlag() {
        return this.unlock;
    }
}
