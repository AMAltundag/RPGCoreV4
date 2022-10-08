package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.hud.editor.bundle.entity.AbstractEditorEntityProvider;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorTotemMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.TotemProxy;

import java.util.List;

/**
 * Living creature that defines position we invoke
 * a skill at, early termination possible thorough
 * death of creature.
 */
public class TotemMechanic extends AbstractCoreMechanic {

    private CoreModifierNumber duration;
    private CoreModifierNumber interval;
    private CoreModifierNumber health;
    private MultiMechanic logic_on_tick;
    private MultiMechanic logic_on_finish;
    private EntityProvider provider;

    public TotemMechanic(EditorTotemMechanic editor) {
        this.duration = editor.duration.build();
        this.interval = editor.interval.build();
        this.health = editor.health.build();
        this.logic_on_tick = editor.logic_on_tick.build();
        this.logic_on_finish = editor.logic_on_finish.build();
        this.provider = ((AbstractEditorEntityProvider) editor.factory.get(0)).build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int duration = this.duration.evalAsInt(context);
        int interval = this.interval.evalAsInt(context);
        int health = this.health.evalAsInt(context);

        for (IOrigin target : targets) {
            TotemProxy proxy = new TotemProxy(context, target, interval, duration, this.provider, this.logic_on_tick, this.logic_on_finish, health);
            context.getCoreEntity().getProxies().add(proxy);
        }
    }
}
