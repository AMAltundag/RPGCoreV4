package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.MechanicConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.mob.ai.CoreMobLogic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EditorMobLogic implements IEditorBundle {

    @EditorWrite(name = "Group", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Shares a namespace with any other Logic.", "Logic sharing a namespace will block each other."})
    public String group = "DEFAULT";
    @EditorList(name = "Condition", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"This behaves like a generic skill selector", "If any target remains, condition is met.", "§cOriginal target is the entity itself."})
    public List<IEditorBundle> start_when_found = new ArrayList<>();
    @EditorList(name = "Execute", constraint = MechanicConstraint.class)
    @EditorTooltip(tooltip = {"Skill mechanics which are invoked if condition is met.", "Target will be the entity itself."})
    public List<IEditorBundle> steps_to_execute = new ArrayList<>();
    @EditorNumber(name = "Cooldown", minimum = 0.0)
    @EditorTooltip(tooltip = {"Cooldown in ticks after logic has been invoked."})
    public double cooldown = 100.0d;
    @EditorNumber(name = "Wait", minimum = 0.0)
    @EditorTooltip(tooltip = {"Time to wait before checking this logic again."})
    public double wait = 20.0;
    @EditorNumber(name = "Priority")
    @EditorTooltip(tooltip = "Sorting criteria in case use multiple logic entries.")
    public double priority = 0.0;
    @EditorList(name = "Selector", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"Transmute original target", "Multi mechanic allows to isolate selectors", "§cOriginal target is the entity itself."})
    public List<IEditorBundle> selector = new ArrayList<>();

    /**
     * Unwrap a list of bundles into mob logic.
     *
     * @param bundles the editor bundles to unwrap
     * @return logic we've constructed
     */
    public static List<CoreMobLogic> unwrap(List<IEditorBundle> bundles) {
        List<CoreMobLogic> unwrapped = new ArrayList<>();
        for (IEditorBundle bundle : bundles) {
            if (bundle instanceof EditorMobLogic) {
                unwrapped.add(new CoreMobLogic((EditorMobLogic) bundle));
            }
        }
        unwrapped.sort(Comparator.comparingDouble(CoreMobLogic::getPriority).reversed());
        return unwrapped;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.COMPARATOR)
                .name("§fMob Logic")
                .appendLore("§fGroup: " + this.group)
                .appendLore("§fConditions: " + this.start_when_found.size() + "X")
                .appendLore("§fExecutes: " + this.steps_to_execute.size() + "X")
                .appendLore("§fCooldown: " + (int) this.cooldown)
                .appendLore("§fWait: " + (int) this.wait)
                .appendLore(String.format("§fPriority: %.2f", this.priority))
                .build();
    }

    @Override
    public String getName() {
        return "Logic";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob Logic");
        instruction.add("The first logic we can find is invoked, priority allows to");
        instruction.add("Check some logic before another. Logic that started will be");
        instruction.add("Expected to finish first.");
        instruction.add("");
        instruction.add("Groups allow you to prevent running conflicting logic at once.");
        return instruction;
    }
}