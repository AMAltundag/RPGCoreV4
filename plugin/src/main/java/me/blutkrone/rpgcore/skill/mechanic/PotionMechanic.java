package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.mechanic.EditorPotionMechanic;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PotionMechanic extends AbstractCoreMechanic {

    private PotionEffectType type;
    private CoreModifierNumber amplifier;
    private CoreModifierNumber duration;

    public PotionMechanic(EditorPotionMechanic editor) {
        this.type = editor.type;
        this.amplifier = editor.amplifier.build();
        this.duration = editor.duration.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        int amplifier = this.amplifier.evalAsInt(context);
        int duration = this.duration.evalAsInt(context);

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity) {
                LivingEntity entity = ((CoreEntity) target).getEntity();
                entity.addPotionEffect(new PotionEffect(type, duration, amplifier, false, false, false));
            }
        }
    }
}
