package me.blutkrone.rpgcore.passive.node;

import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAttributeAndFactor;
import me.blutkrone.rpgcore.hud.editor.bundle.passive.EditorPassiveSkillAttribute;
import me.blutkrone.rpgcore.item.CoreItem;

import java.util.HashMap;
import java.util.Map;

public class CorePassiveSkillAttribute extends AbstractCorePassive {

    private Map<String, Double> factor = new HashMap<>();

    public CorePassiveSkillAttribute(EditorPassiveSkillAttribute editor) {
        for (EditorAttributeAndFactor factor : editor.factors) {
            this.factor.merge(factor.attribute.toLowerCase(), factor.factor, (a,b) -> a+b);
        }
    }

    /**
     * Attributes that we can add to the entity.
     *
     * @return attributes to be gained.
     */
    public Map<String, Double> getAttributes() {
        return factor;
    }

    @Override
    public boolean isSocket() {
        return false;
    }

    @Override
    public boolean canSocket(CoreItem stack) {
        return false;
    }
}
