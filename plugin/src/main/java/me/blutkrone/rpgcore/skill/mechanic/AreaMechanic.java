package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorAreaMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.AreaProxy;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AreaMechanic extends AbstractCoreMechanic {

    private ItemStack item;
    private CoreModifierNumber duration;
    private CoreModifierNumber cooldown;
    private CoreModifierNumber inner_radius;
    private CoreModifierNumber outer_radius;
    private MultiMechanic ticker;
    private MultiMechanic impact;
    private List<AbstractCoreSelector> filter;
    private List<String> effects;

    public AreaMechanic(EditorAreaMechanic editor) {
        this.item = editor.item.isDefault() ? null : editor.item.build();
        this.duration = editor.duration.build();
        this.cooldown = editor.cooldown.build();
        this.inner_radius = editor.inner_radius.build();
        this.outer_radius = editor.outer_radius.build();
        this.ticker = editor.ticker.build();
        this.impact = editor.impact.build();
        this.filter = AbstractEditorSelector.unwrap(editor.filter);
        this.effects = new ArrayList<>(editor.effects);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        int cooldown = this.cooldown.evalAsInt(context);
        double inner_radius = this.inner_radius.evalAsDouble(context);
        double outer_radius = this.outer_radius.evalAsDouble(context);

        for (IOrigin target : targets) {
            AreaProxy proxy = new AreaProxy(context, target, this.item, inner_radius, outer_radius, this.effects, cooldown, duration,
                    this.impact, this.ticker, this.filter);
            context.addProxy(proxy);
        }
    }
}
