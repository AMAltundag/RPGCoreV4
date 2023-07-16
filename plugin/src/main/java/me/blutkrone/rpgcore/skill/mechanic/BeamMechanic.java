package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorBeamMechanic;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.BeamProxy;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Use models to create a "beam", accompany with some
 * particles to assist with visuals. Beam moves with
 * the entity.
 */
public class BeamMechanic extends AbstractCoreMechanic {

    private ItemStack item;
    private CoreModifierBoolean freestyle;
    private CoreModifierNumber rotation_offset;
    private CoreModifierNumber rotation_per_second;
    private CoreModifierNumber range_original;
    private CoreModifierNumber range_per_second;
    private CoreModifierNumber range_maximum;
    private CoreModifierNumber cooldown;
    private CoreModifierNumber duration;
    private MultiMechanic impact;
    private List<AbstractCoreSelector> filter;
    private List<String> beam_effects;
    private List<String> head_effects;

    public BeamMechanic(EditorBeamMechanic editor) {
        this.item = editor.item.build();
        this.freestyle = editor.freestyle.build();
        this.rotation_offset = editor.rotation_offset.build();
        this.rotation_per_second = editor.rotation_per_second.build();
        this.range_original = editor.range_original.build();
        this.range_per_second = editor.range_per_second.build();
        this.range_maximum = editor.range_maximum.build();
        this.cooldown = editor.cooldown.build();
        this.duration = editor.duration.build();
        this.impact = editor.impact.build();
        this.filter = AbstractEditorSelector.unwrap(editor.filter);
        this.beam_effects = new ArrayList<>(editor.beam_effects);
        this.head_effects = new ArrayList<>(editor.head_effects);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        int cooldown = this.cooldown.evalAsInt(context);
        double range_maximum = this.range_maximum.evalAsDouble(context);
        double range_per_second = this.range_per_second.evalAsDouble(context);
        double range_original = this.range_original.evalAsDouble(context);
        float rotation_per_second = (float) this.rotation_per_second.evalAsDouble(context);
        float rotation_offset = (float) this.rotation_offset.evalAsDouble(context);
        boolean freestyle = this.freestyle.evaluate(context);

        for (IOrigin target : targets) {
            BeamProxy proxy = new BeamProxy(context, target, this.item, rotation_offset, rotation_per_second,
                    freestyle, range_original, range_per_second, range_maximum,
                    impact, beam_effects, head_effects, cooldown, duration, filter);
            context.addProxy(proxy);
        }
    }
}
