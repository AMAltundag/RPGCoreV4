package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorAnchorMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.AnchorProxy;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AnchorMechanic extends AbstractCoreMechanic {

    private ItemStack item;
    private CoreModifierNumber duration;
    private MultiMechanic ticker;

    public AnchorMechanic(EditorAnchorMechanic editor) {
        this.item = editor.item.build();
        this.duration = editor.duration.build();
        this.ticker = editor.ticker.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);

        for (IOrigin target : targets) {
            AnchorProxy proxy = new AnchorProxy(context, target, this.item, this.ticker, duration);
            context.getCoreEntity().getProxies().add(proxy);
        }
    }
}
