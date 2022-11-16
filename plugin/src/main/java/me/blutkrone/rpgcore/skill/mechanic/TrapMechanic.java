package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorTrapMechanic;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.AbstractSkillProxy;
import me.blutkrone.rpgcore.skill.proxy.TrapProxy;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TrapMechanic extends AbstractCoreMechanic {

    private ItemStack item;
    private CoreModifierNumber limit;
    private CoreModifierNumber multi;
    private CoreModifierNumber duration;
    private CoreModifierNumber radius;
    private MultiMechanic impact;
    private List<AbstractCoreSelector> filter;

    public TrapMechanic(EditorTrapMechanic editor) {
        this.item = editor.item.build();
        this.limit = editor.limit.build();
        this.multi = editor.multi.build();
        this.duration = editor.duration.build();
        this.radius = editor.radius.build();
        this.impact = editor.impact.build();
        this.filter = AbstractEditorSelector.unwrap(editor.filter);
    }

    /*
     * Counts the trap proxies that the entity has active.
     *
     * @param entity whose trap proxies to count
     * @return how many traps we have
     */
    private int count(CoreEntity entity) {
        int output = 0;
        List<AbstractSkillProxy> proxies = entity.getProxies();
        for (AbstractSkillProxy proxy : proxies) {
            if (proxy instanceof TrapProxy) {
                output += 1;
            }
        }
        return output;
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        double radius = this.radius.evalAsDouble(context);
        double multi = 1d + Math.max(0d, this.multi.evalAsDouble(context));
        int limit = this.limit.evalAsInt(context);
        int active = count(context.getCoreEntity());

        for (IOrigin target : targets) {
            double multi_remaining = multi;
            while (Math.random() < multi_remaining-- && active < limit) {
                TrapProxy proxy = new TrapProxy(context, target, this.item, duration, radius, this.impact, this.filter);
                context.getCoreEntity().getProxies().add(proxy);
                active += 1;
            }
        }
    }
}