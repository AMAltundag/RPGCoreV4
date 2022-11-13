package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorBoltMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.proxy.BoltProxy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplest projectile moving in a straight line, capable
 * of piercing.
 */
public class BoltMechanic extends AbstractCoreMechanic {

    private ItemStack item;
    // private CoreModifierNumber up;
    private CoreModifierNumber pierce;
    private CoreModifierNumber radius;
    private CoreModifierNumber speed;
    private MultiMechanic impact;
    private List<String> effect;

    public BoltMechanic(EditorBoltMechanic editor) {
        this.item = editor.item.build();
        // this.up = editor.up.build();
        this.pierce = editor.pierce.build();
        this.radius = editor.radius.build();
        this.speed = editor.speed.build();
        this.impact = editor.impact.build();
        this.effect = new ArrayList<>(editor.effects);
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int pierce = this.pierce.evalAsInt(context);
        int radius = this.radius.evalAsInt(context);
        int speed = this.speed.evalAsInt(context);

        for (IOrigin target : targets) {
            IOrigin where;
            if (target instanceof CoreEntity) {
                LivingEntity entity = ((CoreEntity) target).getEntity();
                where = new IOrigin.SnapshotOrigin(entity.getEyeLocation());
            } else {
                where = target.isolate();
            }
            BoltProxy proxy = new BoltProxy(context, where, this.item, this.impact, this.effect, pierce, speed, radius);
            context.getCoreEntity().getProxies().add(proxy);
        }
    }
}
