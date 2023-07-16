package me.blutkrone.rpgcore.editor.bundle.other;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.reference.index.MobConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorMobCount implements IEditorBundle {
    @EditorWrite(name = "Mob", constraint = MobConstraint.class)
    @EditorTooltip(tooltip = "Which mob we are counting")
    public String mob = "NOTHINGNESS";
    @EditorNumber(name = "Count")
    @EditorTooltip(tooltip = "Number applied to the mob")
    public double count = 0d;

    /**
     * Useful method to unwrap a list of this class into a map.
     *
     * @param bundles bundles to unwrap
     * @return attribute mapped to factor
     */
    public static Map<String, Integer> unwrap(List<IEditorBundle> bundles) {
        Map<String, Integer> unwrapped = new HashMap<>();
        for (IEditorBundle bundle : bundles) {
            if (bundle instanceof EditorMobCount) {
                String mob = ((EditorMobCount) bundle).mob;
                int count = (int) ((EditorMobCount) bundle).count;
                unwrapped.merge(mob, count, (a, b) -> a + b);
            }
        }
        return unwrapped;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aAttribute And Factor")
                .appendLore("§fMob: " + mob)
                .appendLore("§fCount: " + count)
                .build();
    }

    @Override
    public String getName() {
        return "Mob Count";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob Count");
        instruction.add("Count based on mobs.");
        return instruction;
    }
}
