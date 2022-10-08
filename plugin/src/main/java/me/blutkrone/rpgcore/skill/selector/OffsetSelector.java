package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorOffsetSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class OffsetSelector extends AbstractCoreSelector {

    private CoreModifierNumber forward;
    private CoreModifierNumber upward;

    public OffsetSelector(EditorOffsetSelector editor) {
        this.forward = editor.forward.build();
        this.upward = editor.upward.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        double forward = this.forward.evalAsDouble(context);
        double upward = this.upward.evalAsDouble(context);

        List<IOrigin> output = new ArrayList<>();
        for (IOrigin origin : previous) {
            Location where = origin.getLocation().clone();
            where.add(where.getDirection().multiply(forward));
            where.add(0d, upward, 0d);
            output.add(new IOrigin.SnapshotOrigin(where));
        }
        return output;
    }
}
