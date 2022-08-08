package me.blutkrone.rpgcore.skill;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.root.skill.EditorSkill;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.skill.behaviour.CoreBehaviour;
import me.blutkrone.rpgcore.skill.behaviour.CorePattern;
import me.blutkrone.rpgcore.skill.cost.CoreCost;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierString;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;
import me.blutkrone.rpgcore.skill.skillbar.bound.SkillBindCast;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * The root class of a skill, containing all things that
 * are necessary for a single skill.
 * <p>
 * This is an immutable reflection of the matching editor
 * class, the constructor is intentionally hidden to stop
 * unwanted constructor access.
 */
public class CoreSkill {

    // a unique identifier for the skill
    private final String id;

    // name of the skill
    private String name;
    // describing item of skill
    private ItemStack item;
    // skillbar specific parameters
    private ISkillBind binding;
    // behaviours held while skill is available
    private CoreBehaviour[] behaviours;
    // which type of evolution menu to use
    private String evolution_type;
    // tag list used for evolution filter
    private List<String> evolution_tags;

    public CoreSkill(String id, EditorSkill editor) {
        this.id = id;
        this.name = RPGCore.inst().getLanguageManager()
                .getTranslation(editor.lc_name);
        this.item = RPGCore.inst().getLanguageManager()
                .getAsItem(editor.lc_item)
                .build();
        IChestMenu.setBrand(this.item, RPGCore.inst(), "skill-id", id);

        this.binding = new SkillBindCast() {{
            this.skill = CoreSkill.this;
            this.icon = new CoreModifierString(editor.binding);
            this.cooldown_reduction = new CoreModifierNumber(0d);
            this.cooldown_time = new CoreModifierNumber(100d);
            this.cooldown_recovery = new CoreModifierNumber(0d);
            this.cooldown_id = new CoreModifierString(UUID.randomUUID().toString());
            this.stability = new CoreModifierNumber(0d);
            this.cast_time = new CoreModifierNumber(60d);
            this.cast_faster = new CoreModifierNumber(0d);
            this.costs = new CoreCost[0];
            this.patterns = new CorePattern[0];
        }};
        this.evolution_type = editor.evolution_type;
    }

    /**
     * The itemized equivalent of the skill.
     *
     * @return the itemized equivalent of the skill.
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * A special semi-behaviour which can be triggered by the
     * skillbar, and only by the skillbar.
     *
     * @return the binding configuration of the skill.
     */
    public ISkillBind getBinding() {
        return binding;
    }

    /**
     * The behaviours of this skill.
     *
     * @return behaviours of the skill.
     */
    public CoreBehaviour[] getBehaviours() {
        return behaviours;
    }

    /**
     * The unique ID of the skill, this is an immutable value which
     * cannot be modified by the editor.
     *
     * @return unique skill identifier
     */
    public String getId() {
        return id;
    }

    /**
     * A name used to identify a skill to users, this is just a purely
     * visual hint.
     *
     * @return a name for the skill.
     */
    public String getName() {
        return name;
    }

    /**
     * An evolution type which matches up with the evolution types that
     * the skill menu has. Evolution functionality is disabled when the
     * evolution type cannot be resolved.
     *
     * @return which evolution type to use.
     */
    public String getEvolutionType() {
        return evolution_type;
    }

    /**
     * Tags are used to filter which evolution stone can be fitted into
     * the slots of this skill.
     *
     * @return tag list for evolution
     */
    public List<String> getEvolutionTags() {
        return evolution_tags;
    }
}