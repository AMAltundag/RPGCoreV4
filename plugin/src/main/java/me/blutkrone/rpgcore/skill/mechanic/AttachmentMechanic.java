package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.bbmodel.owner.IModelOwner;
import me.blutkrone.rpgcore.bbmodel.owner.OwnerAttachmentBlock;
import me.blutkrone.rpgcore.bbmodel.owner.OwnerAttachmentEntity;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorAttachmentMechanic;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class AttachmentMechanic extends AbstractCoreMechanic {

    private CoreModifierString id;
    private CoreModifierString model;
    private CoreModifierString animation;
    private CoreModifierNumber speed;
    private CoreModifierNumber duration;
    private CoreModifierNumber size;

    public AttachmentMechanic(EditorAttachmentMechanic editor) {
        this.id = editor.id.build();
        this.model = editor.model.build();
        this.animation = editor.animation.build();
        this.speed = editor.speed.build();
        this.duration = editor.duration.build();
        this.size = editor.size.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        String id = this.id.evaluate(context);
        String model = this.model.evaluate(context);
        String animation = this.animation.evaluate(context);
        double speed = this.speed.evalAsDouble(context);
        int duration = this.duration.evalAsInt(context);
        double size = this.size.evalAsDouble(context);

        for (IOrigin target : targets) {
            if (target instanceof CoreEntity entity) {
                IModelOwner existing = entity.getAttachments().get(id);
                if (existing instanceof OwnerAttachmentEntity oae) {
                    oae.refreshDuration(duration);
                } else if (existing != null) {
                    UUID uuid = UUID.randomUUID();
                    OwnerAttachmentEntity oae = new OwnerAttachmentEntity(entity, model, animation, speed, duration, size);
                    RPGCore.inst().getBBModelManager().priority(uuid, oae);
                    entity.getAttachments().put(id, oae);
                }
            } else {
                Location location = target.getLocation();
                UUID uuid = UUID.randomUUID();
                OwnerAttachmentBlock oae = new OwnerAttachmentBlock(location, model, animation, speed, duration, size);
                RPGCore.inst().getBBModelManager().priority(uuid, oae);
            }
        }
    }
}
