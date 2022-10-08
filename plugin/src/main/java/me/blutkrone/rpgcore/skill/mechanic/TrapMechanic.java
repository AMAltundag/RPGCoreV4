package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorTrapMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.TrapProxy;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TrapMechanic extends AbstractCoreMechanic {

    private ItemStack item;
    private CoreModifierNumber duration;
    private CoreModifierNumber radius;
    private MultiMechanic impact;
    private List<AbstractCoreSelector> filter;

    public TrapMechanic(EditorTrapMechanic editor) {
        this.item = editor.item.build();
        this.duration = editor.duration.build();
        this.radius = editor.radius.build();
        this.impact = editor.impact.build();
        this.filter = AbstractEditorSelector.unwrap(editor.filter);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        double radius = this.radius.evalAsDouble(context);

        for (IOrigin target : targets) {
            TrapProxy proxy = new TrapProxy(context, target, this.item, duration, radius, this.impact, this.filter);
            context.getCoreEntity().getProxies().add(proxy);
        }
    }
}