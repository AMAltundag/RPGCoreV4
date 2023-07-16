package me.blutkrone.rpgcore.skill.mechanic;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.editor.bundle.mechanic.EditorReviveMechanic;
import me.blutkrone.rpgcore.editor.bundle.other.EditorAttributeAndModifier;
import me.blutkrone.rpgcore.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;

import java.util.*;

public class ReviveMechanic extends AbstractCoreMechanic {

    private final CoreModifierNumber minimum_radius;
    private final CoreModifierNumber maximum_radius;
    private final List<AbstractCoreSelector> filter;
    private final CoreModifierNumber health;
    private final CoreModifierNumber mana;
    private final CoreModifierNumber stamina;
    private final Map<String, CoreModifierNumber> attibutes;
    private final CoreModifierNumber duration;

    public ReviveMechanic(EditorReviveMechanic editor) {
        minimum_radius = editor.minimum_radius.build();
        maximum_radius = editor.maximum_radius.build();
        filter = AbstractEditorSelector.unwrap(editor.filter);
        health = editor.health.build();
        mana = editor.mana.build();
        stamina = editor.stamina.build();
        attibutes = EditorAttributeAndModifier.unwrap(editor.attributes);
        duration = editor.duration.build();
    }

    @Override
    public void doMechanic(IContext context, List<IOrigin> targets) {
        double minimum_radius = this.minimum_radius.evalAsDouble(context);
        double maximum_radius = this.maximum_radius.evalAsDouble(context);
        double health = this.health.evalAsDouble(context);
        double mana = this.mana.evalAsDouble(context);
        double stamina = this.stamina.evalAsDouble(context);
        int duration = this.duration.evalAsInt(context);
        Map<String, Double> attributes = new HashMap<>();
        this.attibutes.forEach((id, modifier) -> {
            attributes.put(id, modifier.evalAsDouble(context));
        });

        // find everyone we can resurrect
        Set<IOrigin> filtered = new HashSet<>();
        for (IOrigin target : targets) {
            List<CoreEntity> nearby = target.getNearby(maximum_radius, true);
            nearby.removeIf(entity -> entity.distance(target) >= minimum_radius);
            filtered.addAll(nearby);
        }
        // only retain those who pass filter
        filtered.retainAll(AbstractCoreSelector.doSelect(filter, context, new ArrayList<>(filtered)));
        // offer resurrection to targets
        for (IOrigin entity : filtered) {
            if (entity instanceof CorePlayer) {
                ((CorePlayer) entity).offerToRevive(health, mana, stamina, attributes, duration);
            }
        }
    }
}
