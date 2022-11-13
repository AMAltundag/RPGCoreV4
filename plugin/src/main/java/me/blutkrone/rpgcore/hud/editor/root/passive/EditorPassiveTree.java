package me.blutkrone.rpgcore.hud.editor.root.passive;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.passive.CorePassiveTree;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorPassiveTree implements IEditorRoot<CorePassiveTree> {

    @EditorWrite(name = "Point", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Points utilized on this tree", "Can be pooled across trees"})
    public String point = "default";
    @EditorWrite(name = "Menu", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"A menu texture to use as the backdrop"})
    public String menu_design = "default";
    @EditorBoolean(name = "Fixed X-Axis")
    @EditorTooltip(tooltip = {"Prevents viewport of X being anything but 0"})
    public boolean locked_x = false;
    @EditorBoolean(name = "Fixed Y-Axis")
    @EditorTooltip(tooltip = {"Prevents viewport of Y being anything but 0"})
    public boolean locked_y = false;
    @EditorBoolean(name = "Portrait")
    @EditorTooltip(tooltip = {"Portrait to use when viewed in job menu."})
    public String job_portrait = "tree_nothing";
    @EditorBoolean(name = "Category")
    @EditorTooltip(tooltip = {"Category portrait, only shown with multiple categories."})
    public String job_category = "tree_nothing";

    // special case, layout editing is integrated into the menu
    public Map<String, List<Long>> layout = new HashMap<>();
    // integrity performs a lazy reset on passive tree
    public long integrity = System.currentTimeMillis();

    public transient File file;

    public EditorPassiveTree() {
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
    public CorePassiveTree build(String id) {
        return new CorePassiveTree(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.END_CRYSTAL)
                .name("§aPassive Tree")
                .build();
    }

    @Override
    public String getName() {
        return "Tree";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Passive Tree");
        instruction.add("Holds the layout for a passive tree, along with");
        instruction.add("General information used by it.");
        instruction.add("");
        instruction.add("You can edit the layout if you open the tree, you can");
        instruction.add("Shift-Right click to manipulate the node there.");
        instruction.add("");
        instruction.add("Only 3 job trees show on a category, the player will not");
        instruction.add("Be able to access a 4th tree of the same category. Category");
        instruction.add("Is only shown if you have more then 3 trees total.");
        instruction.add("");
        instruction.add("§cAltering the passive tree will reset any allocations!");
        instruction.add("§cA tree will not affect a player by itself!");
        return instruction;
    }
}
