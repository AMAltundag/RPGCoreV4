package me.blutkrone.rpgcore.hud.editor.root.item;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorHideWhen;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.*;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorAffixChance;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorAffixLimit;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.AffixChanceConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.AffixLimitConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.ItemTypeConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.MaterialConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.ItemStyleConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.ImplicitModifierConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.type.ItemType;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorItem implements IEditorRoot<CoreItem> {

    @EditorCategory(icon = Material.CRAFTING_TABLE, info = {"Appearance", "How to present the item to a player"})
    @EditorWrite(name = "Material", constraint = MaterialConstraint.class)
    public Material material = Material.IRON_PICKAXE;
    @EditorWrite(name = "Text", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Name, Sub-Name, Description", "§cThis is a language code, NOT plaintext."})
    public String lc_text = "NOTHINGNESS";
    @EditorWrite(name = "Info", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Additional Information", "§cThis is a language code, NOT plaintext."})
    public String lc_info = "NOTHINGNESS";
    @EditorNumber(name = "Model", minimum = 0)
    public double model_data = 0;
    @EditorColor(name = "Color")
    @EditorTooltip(tooltip = "Item Color, also used by custom armors.")
    @EditorHideWhen(field = "material", value = {"LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS"}, invert = true)
    public String color_data = "FFFFFF";

    @EditorCategory(icon = Material.IRON_PICKAXE, info = {"Item Type", "Specialise the purpose of the item"})
    @EditorWrite(name = "Type", constraint = ItemTypeConstraint.class)
    public ItemType item_type = ItemType.NONE;
    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Tags which the item can be addressed with", "The ID 'DIRECT_<id>' is always generated"})
    public List<String> tags = new ArrayList<>();

    @EditorCategory(icon = Material.ANVIL, info = {"Durability", "Configure durability and repairing"})
    @EditorNumber(name = "Durability")
    @EditorTooltip(tooltip = "RPGCore custom item durability")
    public double durability = 0;
    @EditorNumber(name = "Repairing", minimum = 0)
    @EditorTooltip(tooltip = "Grade of repair fodder necessary to fix item.")
    @EditorHideWhen(field = "durability", value = "0")
    public double repair_grade = 0;

    @EditorCategory(icon = Material.FURNACE, info = {"Modifiers", "Modifiers acquired while holding item"})
    @EditorList(name = "Implicits", constraint = ImplicitModifierConstraint.class)
    @EditorTooltip(tooltip = "IDs of modifiers to always utilize")
    public List<String> implicits = new ArrayList<>();
    @EditorNumber(name = "Level", minimum = 0)
    @EditorTooltip(tooltip = "Level used to constrain affix generation")
    public double affix_level = 0;
    @EditorList(name = "Limits", constraint = AffixLimitConstraint.class)
    @EditorTooltip(tooltip = "Limit affixes by their shared tags")
    @EditorHideWhen(field = "affix_level", value = "0")
    public List<EditorAffixLimit> affix_limit = new ArrayList<>();
    @EditorList(name = "Chance", constraint = AffixChanceConstraint.class)
    @EditorTooltip(tooltip = "Change affix likelihood by their shared tags")
    @EditorHideWhen(field = "affix_level", value = "0")
    public List<EditorAffixChance> affix_chance = new ArrayList<>();

    @EditorCategory(icon = Material.FURNACE, info = {"Jewels", "Socket jewels on items for extra attributes"})
    @EditorNumber(name = "Maximum", minimum = 0)
    @EditorTooltip(tooltip = "How many jewels can be socketed on the item")
    public double jewel_maximum = 0;
    @EditorList(name = "Accepted", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "What equipment tags can be slotted into the sockets")
    @EditorHideWhen(field = "jewel_maximum", value = {"0"})
    public List<String> jewel_accept = new ArrayList<>();
    @EditorWrite(name = "Icon", constraint = ItemStyleConstraint.class)
    @EditorTooltip(tooltip = "An icon to represent this item while socketed")
    public String jewel_icon = "default";
    @EditorNumber(name = "Shatter", minimum = 0d, maximum = 1d)
    @EditorTooltip(tooltip = {"Chance for other jewels to shatter when this one is socketed", "This is rolled once for each prior jewel!"})
    public double jewel_shatter = 0.0d;
    @EditorNumber(name = "Previous", minimum = 0d)
    @EditorTooltip(tooltip = {"Minimum number of previously socketed jewels, to socket this one."})
    public int jewel_previous = 0;

    @EditorCategory(icon = Material.FURNACE, info = {"Animation", "Animations related to the item"})
    @EditorWrite(name = "Icon", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Animation used when jewel is shattered")
    public String animation_shatter = "shatter_default";

    @EditorCategory(icon = Material.FURNACE, info = {"Banking", "Allows storage of large item quantity."})
    @EditorWrite(name = "Group", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "All items sharing the same group are pooled together")
    public String bank_group = "none";
    @EditorNumber(name = "Value", minimum = 1)
    @EditorTooltip(tooltip = "Quantity provided to the banked group")
    public double bank_quantity = 1.0d;
    @EditorWrite(name = "Symbol", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Symbol used when rendered")
    public String bank_symbol = "default";

    @EditorCategory(icon = Material.BOOKSHELF, info = {"Others", "Additional configurations"})
    @EditorBoolean(name = "Bounded")
    @EditorTooltip(tooltip = "Item is can only be used by one player.")
    public boolean drop_bound = false;
    @EditorBoolean(name = "Unstackable")
    @EditorTooltip(tooltip = "Disables item from stacking")
    public boolean do_not_stack = false;
    @EditorList(name = "Equipment", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Which equipment slots we can equip into.")
    public List<String> equipment_slot = new ArrayList<>();
    @EditorNumber(name = "Score", minimum = 1)
    @EditorTooltip(tooltip = "Item level used to gauge power of this item.")
    public double item_level = 0;
    @EditorWrite(name = "Styling", constraint = ItemStyleConstraint.class)
    @EditorTooltip(tooltip = "Which styling rule to apply on the item")
    public String lore_style = "normal";

    private transient File file;
    private transient ItemStack preview;

    public EditorItem() {
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
    public CoreItem build(String id) {
        // construct a runtime instance which can be used
        CoreItem item = new CoreItem(id, this);
        // create a snapshot to respect existing logic
        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            try {
                this.preview = item.acquire(null, 0d);
            } catch (Exception e) {

            }
        });
        // offer up the item which we generated
        return item;
    }

    @Override
    public ItemStack getPreview() {
        if (this.preview == null) {
            return ItemBuilder.of(Material.BARRIER)
                    .name("§cNo preview was generated yet!")
                    .appendLore("§fForce a preview to generate by saving!")
                    .build();
        } else {
            return this.preview.clone();
        }
    }

    @Override
    public String getName() {
        return RPGCore.inst().getLanguageManager().getTranslation(this.lc_text);
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fItem");
        instruction.add("An item which can engage with core entities");
        return instruction;
    }
}
