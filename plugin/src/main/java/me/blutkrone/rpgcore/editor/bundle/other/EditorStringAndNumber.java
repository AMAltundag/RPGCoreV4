package me.blutkrone.rpgcore.editor.bundle.other;

import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EditorStringAndNumber implements IEditorBundle {

    @EditorWrite(name = "String", constraint = StringConstraint.class)
    public String string = "nothing";
    @EditorNumber(name = "Number")
    public double number = 0.0;

    public EditorStringAndNumber() {
    }

    public static Map<Integer, Set<String>> transformIntegerToStringList(List<IEditorBundle> editor) {
        Map<Integer, Set<String>> output = new HashMap<>();
        for (IEditorBundle bundle : editor) {
            EditorStringAndNumber casted = (EditorStringAndNumber) bundle;
            output.computeIfAbsent((int) casted.number, (k -> new HashSet<>())).add(casted.string);
        }
        return output;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("Pair")
                .appendLore("String: " + this.string)
                .appendLore("Number: " + this.number)
                .build();
    }

    @Override
    public String getName() {
        return "Pair";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("String And Number");
        instruction.add("A string associated with a number.");
        return instruction;
    }
}
