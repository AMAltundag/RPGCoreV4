package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorChainMechanic;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.ChainProxy;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Projectile which automatically moves between targets,
 * will never strike multiple times.
 */
public class ChainMechanic extends AbstractCoreMechanic {

    private CoreModifierNumber chains;
    private CoreModifierNumber delay;
    private CoreModifierNumber radius;
    private MultiMechanic impact;
    private List<AbstractCoreSelector> filter;
    private List<String> effects;

    public ChainMechanic(EditorChainMechanic editor) {
        this.chains = editor.chains.build();
        this.delay = editor.delay.build();
        this.radius = editor.radius.build();
        this.impact = editor.impact.build();
        this.filter = AbstractEditorSelector.unwrap(editor.filter);
        this.effects = new ArrayList<>(editor.effects);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int chains = this.chains.evalAsInt(context);
        int delay = this.delay.evalAsInt(context);
        double radius = this.radius.evalAsDouble(context);

        for (IOrigin target : targets) {
            IOrigin where;
            if (target instanceof CoreEntity) {
                LivingEntity entity = ((CoreEntity) target).getEntity();
                where = new IOrigin.SnapshotOrigin(entity.getEyeLocation());
            } else {
                where = target.isolate();
            }

            ChainProxy proxy = new ChainProxy(context, where, this.impact, this.effects, chains, delay, radius, filter);
            context.addProxy(proxy);
        }
    }
}
