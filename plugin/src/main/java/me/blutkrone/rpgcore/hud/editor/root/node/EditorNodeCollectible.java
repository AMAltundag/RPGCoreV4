package me.blutkrone.rpgcore.hud.editor.root.node;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.LootConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.MaterialConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.AttributeConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.node.impl.CoreNodeCollectible;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * A node refers to a specific point on the map which
 * serve as a point-of-interest to a player.
 */
public class EditorNodeCollectible implements IEditorRoot<CoreNodeCollectible> {

    @EditorNumber(name = "Radius", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"Radius which the node can trigger at."})
    public double radius = 16;
    @EditorList(name = "Weight", constraint = LootConstraint.class)
    @EditorTooltip(tooltip = "What items can be gained from collecting.")
    public List<IEditorBundle> item_weight = new ArrayList<>();
    @EditorNumber(name = "Total", minimum = 0, maximum = 48)
    @EditorTooltip(tooltip = {"How many items will be dropped."})
    public double item_total = 2;
    @EditorNumber(name = "Cooldown", minimum = 0)
    @EditorTooltip(tooltip = {"Time to recover for another usage."})
    public double cooldown = 2;

    @EditorCategory(icon = Material.GREEN_WOOL, info = "Collection")
    @EditorNumber(name = "Size", minimum = 1, maximum = 127)
    @EditorTooltip(tooltip = {"Size of collider box."})
    public double collide_size = 3;
    @EditorNumber(name = "Volume", minimum = 1, maximum = 999)
    @EditorTooltip(tooltip = {"Hits necessary to collect the node."})
    public double collect_volume = 3;
    @EditorWrite(name = "Attribute", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = {"Increases the value of each your hits.."})
    public String collect_attribute = "NOTHINGNESS";
    @EditorWrite(name = "Attribute", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = {"Message shown while collecting the node.", "§cThis is a language code, NOT plaintext."})
    public String lc_message = "NOTHINGNESS";

    @EditorCategory(icon = Material.GREEN_WOOL, info = "Available Model")
    @EditorWrite(name = "Material", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = {"Icon used while node is available."})
    public Material available_icon = Material.STONE_PICKAXE;
    @EditorNumber(name = "Model")
    @EditorTooltip(tooltip = {"Model used while node is available"})
    public double available_model = 0;

    @EditorCategory(icon = Material.GRAY_WOOL, info = "Unavailable Model")
    @EditorWrite(name = "Material", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = {"Icon used while node is not available."})
    public Material unavailable_icon = Material.STONE_PICKAXE;
    @EditorNumber(name = "Model", minimum = 0)
    @EditorTooltip(tooltip = {"Model used while node is not available"})
    public double unavailable_model = 0;

    @EditorCategory(icon = Material.GREEN_WOOL, info = "Effects")
    @EditorWrite(name = "Collecting", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = {"Effect invoked while actively collecting."})
    public String effect_collecting = "NOTHINGNESS";
    @EditorWrite(name = "Collected", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = {"Effect invoked when finished collecting."})
    public String effect_collected = "NOTHINGNESS";

    public transient File file;

    public EditorNodeCollectible() {
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
    public CoreNodeCollectible build(String id) {
        return new CoreNodeCollectible(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aCollectible Node")
                .appendLore("§fTotal: " + this.item_total)
                .appendLore("§fWeights: X" + this.item_weight.size())
                .build();
    }

    @Override
    public String getName() {
        return "Collectible";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bCollectible Node");
        instruction.add("Spawns a physical item which can be harvested.");
        return instruction;
    }
}
