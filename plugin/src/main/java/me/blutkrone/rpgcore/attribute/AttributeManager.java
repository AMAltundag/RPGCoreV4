package me.blutkrone.rpgcore.attribute;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.damage.ailment.AbstractAilment;
import me.blutkrone.rpgcore.damage.ailment.ailments.AttributeAilment;
import me.blutkrone.rpgcore.damage.ailment.ailments.DamageAilment;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.other.EditorAttribute;
import org.bukkit.Bukkit;

import java.util.Map;

/**
 * Manages attributes on the server, do note that despite everyone
 * having access to all attributes they aren't obliged to utilize
 * them.
 * <p>
 * Under no circumstances should an attribute be EVER removed after
 * it has been created!
 * <p>
 *
 * @see AttributeCollection interface needed to hold attributes
 */
public class AttributeManager {

    // attributes registered to the core
    private EditorIndex<CoreAttribute, EditorAttribute> index;

    public AttributeManager() {
        this.index = new EditorIndex<>("attribute", EditorAttribute.class, EditorAttribute::new);

        // initialize all attributes that shouldn't be zero
        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            // ailment threshold
            tryToInit("AILMENT_THRESHOLD", 0.07d, "Attribute '%s' defaults to zero (This prevents ailments from applying!)");
            // maximum recoup rate
            tryToInit("HEALTH_LEECH_MAXIMUM", 0.25d, "Attribute '%s' defaults to zero (This prevents recoup from applying!)");
            tryToInit("MANA_LEECH_MAXIMUM", 0.25d, "Attribute '%s' defaults to zero (This prevents recoup from applying!)");
            tryToInit("STAMINA_LEECH_MAXIMUM", 0.25d, "Attribute '%s' defaults to zero (This prevents recoup from applying!)");
            // maximum leech rate
            tryToInit("HEALTH_RECOUP_PER_SECOND", 0.25d, "Attribute '%s' defaults to zero (This prevents recoup from applying!)");
            tryToInit("MANA_RECOUP_PER_SECOND", 0.25d, "Attribute '%s' defaults to zero (This prevents recoup from applying!)");
            tryToInit("STAMINA_RECOUP_PER_SECOND", 0.25d, "Attribute '%s' defaults to zero (This prevents recoup from applying!)");
            // element related attributes
            for (DamageElement element : RPGCore.inst().getDamageManager().getElements()) {
                tryToInit(element.getMaxReductionAttribute(), 0.75d, "Attribute '%s' defaults to zero (This prevents resistances from applying!)");
                tryToInit(element.getMinimumRange(), 0.8d, "Attribute '%s' defaults to zero (This prevents damage from applying!)");
                tryToInit(element.getMaximumRange(), 1.0d, "Attribute '%s' defaults to zero (This prevents damage from applying!)");
            }
            // just warn about ailment contribution
            for (AbstractAilment ailment : RPGCore.inst().getDamageManager().getAilments()) {
                if (ailment instanceof AttributeAilment) {
                    Map<String, String> contribution = ((AttributeAilment) ailment).contribution;
                    String attribute = contribution.get(((AttributeAilment) ailment).element);
                    if (attribute != null) {
                        tryToInit(attribute, 0.1d, "Attribute '%s' defaults to zero (This prevents ailments from applying!)");
                    } else {
                        Bukkit.getLogger().severe("Ailment with element " + ((AttributeAilment) ailment).element + " has no contribution attribute!");
                    }
                } else if (ailment instanceof DamageAilment) {
                    Map<String, String> contribution = ((DamageAilment) ailment).contribution;
                    String attribute = contribution.get(((DamageAilment) ailment).element);
                    if (attribute != null) {
                        tryToInit(attribute, 0.1d, "Attribute '%s' defaults to zero (This prevents ailments from applying!)");
                    } else {
                        Bukkit.getLogger().severe("Ailment with element " + ((DamageAilment) ailment).element + " has no contribution attribute!");
                    }
                }
            }
        });
    }

    /**
     * The index which tracks our attributes.
     *
     * @return the index
     */
    public EditorIndex<CoreAttribute, EditorAttribute> getIndex() {
        return index;
    }

    /*
     * Attempt to initialise attribute to default value
     *
     * @param attribute What attribute to initialise
     * @param defaults What value to initialise to
     * @param warning The warning to show be shown
     */
    private void tryToInit(String attribute, double defaults, String warning) {
        try {
            double snapshot = defaults;
            RPGCore.inst().getAttributeManager().getIndex().create(attribute, (attr -> {
                attr.defaults = snapshot;
            }));
        } catch (Exception e) {
            defaults = RPGCore.inst().getAttributeManager().getIndex().get(attribute).getDefaults();
            if (defaults == 0d) {
                RPGCore.inst().getLogger().warning(warning.formatted(attribute));
            }
        }
    }
}