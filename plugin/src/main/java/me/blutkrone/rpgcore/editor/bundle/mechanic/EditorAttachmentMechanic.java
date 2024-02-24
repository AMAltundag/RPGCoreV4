package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.AttachmentMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorAttachmentMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "ID")
    @EditorTooltip(tooltip = {"If attachment exists, will refresh duration instead", "Only works if attached to an entity."})
    public EditorModifierString id = new EditorModifierString(UUID.randomUUID().toString());
    @EditorBundle(name = "Model")
    @EditorTooltip(tooltip = {"Either an item a blockbench model ID", "A model will play its death animation before removal"})
    public EditorModifierString model = new EditorModifierString("nothingness");
    @EditorBundle(name = "Animation")
    @EditorTooltip(tooltip = {"ID of the animation to play", "Completion takes precedence over duration", "No precedence if 'loop' or 'hold'", "§cMust reference a blockbench model!"})
    public EditorModifierString animation = new EditorModifierString("nothingness");
    @EditorBundle(name = "Speed")
    @EditorTooltip(tooltip = {"Playback speed of the animations", "§cMust reference a blockbench model!"})
    public EditorModifierNumber speed = new EditorModifierNumber(1.0f);
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = {"When out of duration, will be queued for removal"})
    public EditorModifierNumber duration = new EditorModifierNumber(100);
    @EditorBundle(name = "Size")
    @EditorTooltip(tooltip = {"Size of the attached model"})
    public EditorModifierNumber size = new EditorModifierNumber(1.0f);

    @Override
    public AbstractCoreMechanic build() {
        return new AttachmentMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.SADDLE)
                .name("§fAttachment")
                .build();
    }

    @Override
    public String getName() {
        return "Attachment";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Attachment");
        instruction.add("Assign a temporary model to follow the target.");
        instruction.add("");
        instruction.add("If following an entity, duration runs out if the entity");
        instruction.add("Ends up dying.");
        instruction.add("");
        instruction.add("If following a non-entity, will instead be fixated");
        instruction.add("At the given location.");

        return instruction;
    }
}
