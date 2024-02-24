package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.bbmodel.owner.IModelOwner;
import me.blutkrone.rpgcore.editor.bundle.selector.EditorModelSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ModelSelector extends AbstractCoreSelector {

    private final CoreModifierString bone;
    private final CoreModifierBoolean normalized;

    public ModelSelector(EditorModelSelector editor) {
        this.bone = editor.bone.build();
        this.normalized = editor.normalized.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        String bone = this.bone.evaluate(context);
        boolean normalized = this.normalized.evaluate(context);

        List<IOrigin> output = new ArrayList<>();
        for (IOrigin origin : previous) {
            if (origin instanceof CoreEntity e) {
                Location location = e.getHeadLocation();

                IModelOwner model = RPGCore.inst().getBBModelManager().get(e);
                if (model != null) {
                    location = model.getLocation(bone, normalized);
                }

                output.add(new IOrigin.SnapshotOrigin(location));
            } else {
                output.add(origin);
            }
        }

        return output;
    }
}