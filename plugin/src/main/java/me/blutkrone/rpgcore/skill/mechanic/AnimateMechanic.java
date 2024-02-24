package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.bbmodel.owner.IActiveOwner;
import me.blutkrone.rpgcore.bbmodel.owner.IModelOwner;
import me.blutkrone.rpgcore.bbmodel.util.exception.BBExceptionRecycled;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorModelAnimateMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import org.bukkit.Bukkit;

import java.util.List;

public class AnimateMechanic extends AbstractCoreMechanic {

    private final CoreModifierString animation;
    private final CoreModifierNumber speed;
    private final CoreModifierBoolean stop;
    private final CoreModifierBoolean fade;

    public AnimateMechanic(EditorModelAnimateMechanic editor) {
        this.animation = editor.animation.build();
        this.speed = editor.speed.build();
        this.stop = editor.stop.build();
        this.fade = editor.fade.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        String animation = this.animation.evaluate(context);
        float speed = (float) this.speed.evalAsDouble(context);
        boolean stop = this.stop.evaluate(context);
        boolean fade = this.fade.evaluate(context);

        for (IOrigin target : targets) {
            if (target instanceof CoreMob e) {
                IModelOwner owner = RPGCore.inst().getBBModelManager().get(e);
                if (owner instanceof IActiveOwner model) {
                    try {
                        if (stop) {
                            model.stopAnimation(animation);
                        } else if (fade) {
                            model.fadeAnimation(animation);
                        } else {
                            model.playAnimation(animation, speed);
                        }
                    } catch (BBExceptionRecycled ex) {
                        Bukkit.getLogger().severe("Model for " + target + " was already recycled!");
                    }
                }
            }
        }
    }
}
