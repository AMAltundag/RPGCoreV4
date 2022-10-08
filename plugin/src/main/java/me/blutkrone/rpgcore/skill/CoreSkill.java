package me.blutkrone.rpgcore.skill;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.binding.AbstractEditorSkillBinding;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorBehaviour;
import me.blutkrone.rpgcore.hud.editor.root.skill.EditorSkill;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.skill.behaviour.CoreBehaviour;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
    private List<CoreBehaviour> behaviours;
    // which type of evolution menu to use
    private String evolution_type;
    // tag list used for evolution filter
    private List<String> evolution_tags;

    public CoreSkill(String id, EditorSkill editor) {
        this.id = id;
        this.evolution_type = editor.evolution_type;
        this.name = RPGCore.inst().getLanguageManager()
                .getTranslation(editor.lc_name);
        this.item = RPGCore.inst().getLanguageManager()
                .getAsItem(editor.lc_item)
                .build();
        IChestMenu.setBrand(this.item, RPGCore.inst(), "skill-id", id);
        this.behaviours = new ArrayList<>();
        for (IEditorBundle bundle : editor.behaviours) {
            this.behaviours.add(((EditorBehaviour) bundle).build(this));
        }
        for (IEditorBundle bundle : editor.skill_binding) {
            this.binding = ((AbstractEditorSkillBinding) bundle).build(this);
        }

        Bukkit.getLogger().severe("not implemented (passive behaviours are never assigned)");
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
    public List<CoreBehaviour> getBehaviours() {
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