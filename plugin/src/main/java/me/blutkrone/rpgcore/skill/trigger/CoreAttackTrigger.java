package me.blutkrone.rpgcore.skill.trigger;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.trigger.EditorAttackTrigger;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.skill.SkillContext;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * Triggers when performing an attack with a weapon, i.E.: Melee
 * attack or shooting a bow.
 */
public class CoreAttackTrigger extends AbstractCoreTrigger {

    // the tags on the weapon item
    private Set<String> types;

    public CoreAttackTrigger(EditorAttackTrigger editor) {
        super(editor);

        this.types = new HashSet<>(editor.types);

        this.cooldown_recovery = editor.cooldown_recovery.build();
        this.cooldown_reduction = editor.cooldown_reduction.build();
        this.cooldown_time = editor.cooldown_time.build();
        this.cooldown_id = editor.cooldown_id.build();
    }

    @Override
    public boolean update(SkillContext context, Object event, TriggerInfo info) {
        if (!(event instanceof ItemStack)) {
            return false;
        }
        // check for tags of the weapons
        if (!this.types.isEmpty()) {
            // list up the tags which we have
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(((ItemStack) event), ItemDataGeneric.class);
            Set<String> types = new HashSet<>();
            types.add(data.getItem().getId().toLowerCase());
            for (String type : data.getItem().getTags()) {
                types.add(type.toLowerCase());
            }
            // ensure check if match with any tag
            boolean matched = false;
            for (String tag : this.types) {
                if (types.contains(tag)) {
                    matched = true;
                }
            }
            // cancel if we do not match any tag
            if (!matched) {
                return false;
            }
        }
        // update hit-count and check for enough hits
        return true;
    }
}
