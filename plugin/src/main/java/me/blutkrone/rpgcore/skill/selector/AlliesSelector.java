package me.blutkrone.rpgcore.skill.selector;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.EditorAlliesSelector;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;

import java.util.*;

public class AlliesSelector extends AbstractCoreSelector {

    private CoreModifierNumber radius;
    private CoreModifierNumber total;

    public AlliesSelector(EditorAlliesSelector editor) {
        radius = editor.radius.build();
        total = editor.total.build();
    }

    @Override
    public List<IOrigin> doSelect(IContext context, List<IOrigin> previous) {
        double radius = this.radius.evalAsDouble(context);
        int total = this.total.evalAsInt(context);

        // search entities within the radius
        Set<CoreEntity> discovered = new HashSet<>();
        for (IOrigin target : previous) {
            discovered.addAll(target.getNearby(radius));
        }
        // trim to only allied targets
        discovered.removeIf(entity -> {
            return !entity.isFriendly(context.getCoreEntity());
        });
        // retain only the relevant targets
        List<IOrigin> sorted = new ArrayList<>(discovered);
        if (sorted.size() > total) {
            Collections.shuffle(sorted);
            sorted = sorted.subList(0, total);
        }

        return sorted;
    }
}
