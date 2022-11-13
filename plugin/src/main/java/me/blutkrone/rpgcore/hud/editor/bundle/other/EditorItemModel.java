package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorColor;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.MaterialConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorItemModel implements IEditorBundle {

    @EditorWrite(name = "Material", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = {"Material used for the item"})
    public Material material = Material.IRON_AXE;
    @EditorNumber(name = "Model", minimum = 0, maximum = 9999999)
    @EditorTooltip(tooltip = {"Custom item model"})
    public double model = -1;
    @EditorColor(name = "Color")
    @EditorTooltip(tooltip = {"Color to use for the item.", "Not all items can be colored!"})
    public String color = "FFFFFF";
    @EditorBoolean(name = "Enchanted")
    @EditorTooltip(tooltip = {"Gives the item an enchanted look."})
    public boolean enchanted = false;

    public boolean isDefault() {
        return this.material == Material.IRON_AXE
                && this.model == -1
                && this.color.equalsIgnoreCase("FFFFFF")
                && !this.enchanted;
    }

    public ItemStack build() {
        if (this.enchanted) {
            return ItemBuilder.of(material)
                    .color(Integer.parseInt(color, 16))
                    .model(((int) this.model))
                    .enchant(Enchantment.DURABILITY, 1)
                    .name("§fItem Model")
                    .build();
        } else {
            return ItemBuilder.of(material)
                    .color(Integer.parseInt(color, 16))
                    .model(((int) this.model))
                    .name("§fItem Model")
                    .build();
        }
    }

    @Override
    public ItemStack getPreview() {
        if (this.enchanted) {
            return ItemBuilder.of(material)
                    .color(Integer.parseInt(color, 16))
                    .model(((int) this.model))
                    .enchant(Enchantment.DURABILITY, 1)
                    .name("§fItem Model")
                    .build();
        } else {
            return ItemBuilder.of(material)
                    .color(Integer.parseInt(color, 16))
                    .model(((int) this.model))
                    .name("§fItem Model")
                    .build();
        }
    }

    @Override
    public String getName() {
        return "Item Model";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Item Model");
        instruction.add("An item which serves purely visual function, the");
        instruction.add("Item should never appear in a player inventory.");
        return instruction;
    }
}
