package me.blutkrone.rpgcore.skill;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.binding.AbstractEditorSkillBinding;
import me.blutkrone.rpgcore.editor.bundle.other.EditorBehaviour;
import me.blutkrone.rpgcore.editor.bundle.other.EditorSkillInfo;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.item.EditorItem;
import me.blutkrone.rpgcore.editor.root.passive.EditorPassiveTree;
import me.blutkrone.rpgcore.editor.root.skill.EditorSkill;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.styling.IDescriptionRequester;
import me.blutkrone.rpgcore.item.type.ItemType;
import me.blutkrone.rpgcore.passive.CorePassiveTree;
import me.blutkrone.rpgcore.skill.behaviour.CoreBehaviour;
import me.blutkrone.rpgcore.skill.info.CoreSkillInfo;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
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
    // skillbar specific parameters
    private ISkillBind binding;
    // behaviours held while skill is available
    private List<CoreBehaviour> behaviours;
    // tags that identify the skill
    private List<String> tags;
    // hide from player until skill unlocked
    private boolean hidden;
    // if non-hidden passive is held permanently
    private boolean passive;
    // info modifiers regarding the skill
    private List<CoreSkillInfo> info;

    public CoreSkill(String id, EditorSkill editor) {
        this.id = id;
        this.name = editor.lc_name;
        this.tags = new ArrayList<>(editor.tags);
        this.tags.replaceAll(String::toLowerCase);
        this.behaviours = new ArrayList<>();
        for (IEditorBundle bundle : editor.behaviours) {
            this.behaviours.add(((EditorBehaviour) bundle).build(this));
        }
        for (IEditorBundle bundle : editor.skill_binding) {
            this.binding = ((AbstractEditorSkillBinding) bundle).build(this);
        }
        this.hidden = editor.hidden;
        this.passive = editor.passive;
        this.info = new ArrayList<>();
        if (this.binding != null) {
            this.info.addAll(this.binding.getInfo());
        }
        for (EditorSkillInfo bundle : editor.info_modifiers) {
            this.info.add(bundle.build());
        }
        // a has some external dependencies on it
        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            // create a item dedicated to the skill
            EditorIndex<CoreItem, EditorItem> item_index = RPGCore.inst().getItemManager().getItemIndex();
            if (!item_index.has("skill_" + id)) {
                item_index.create("skill_" + id, (created -> {
                    created.hidden_data.put("skill", id);
                    created.lc_info = "NOTHINGNESS";
                    created.lc_text = "NOTHINGNESS";
                    created.do_not_stack = true;
                    created.item_level = 1;
                    created.item_type = ItemType.NONE;
                    created.lore_style = "skill";
                    created.tags = new ArrayList<>(Collections.singletonList("RPG_SKILL"));
                }));
            }

            // create a tree dedicated to the skill
            EditorIndex<CorePassiveTree, EditorPassiveTree> skill_index = RPGCore.inst().getPassiveManager().getTreeIndex();
            if (!skill_index.has("skill_" + id)) {
                skill_index.create("skill_" + id, (created -> {
                    created.point = "skill_" + id;
                }));
            }
        });
    }

    /**
     * Information on how skill will behave, we want this mainly
     * for the itemization of skills.
     *
     * @return information about the skill.
     */
    public List<CoreSkillInfo> getInfo() {
        return new ArrayList<>(info);
    }

    /**
     * Check if we can access this skill passively, do note that
     * the player may have active access (bound to skillbar.)
     *
     * @param player who to check
     * @return whether we have passive access
     */
    public boolean hasPassiveAccess(CorePlayer player) {
        // check if we can have passive access
        if (!this.isPassive()) {
            return false;
        }
        // check if the skill is hidden to the player
        if (this.isHidden() && !player.checkForTag("skill_" + this.getId())) {
            return false;
        }
        // player has access
        return true;
    }

    /**
     * If not hidden to a player, will gain all passive behaviours.
     *
     * @return whether to gain all passive behaviours if not passive
     */
    public boolean isPassive() {
        return passive;
    }

    /**
     * Whether to hide the skill from menus and skillbar until we
     * have an external source (items, passives, etc) that grants
     * access to it.
     *
     * @return whether the skill is hidden.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * The itemized equivalent of the skill.
     *
     * @return the itemized equivalent of the skill.
     */
    public ItemStack getItem(IDescriptionRequester player) {
        return RPGCore.inst().getItemManager().getItemIndex()
                .get("skill_" + getId())
                .acquire(player, 0.0d);
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
        return RPGCore.inst().getLanguageManager().getTranslation(this.name);
    }

    /**
     * Secondary name, mainly for itemized skill.
     *
     * @return secondary skill name.
     */
    public String getSubName() {
        List<String> line = RPGCore.inst().getLanguageManager().getTranslationList(this.name);
        return line.size() > 1 ? line.get(1) : "";
    }

    /**
     * General description of the skill.
     *
     * @return skill description
     */
    public List<String> getDescription() {
        List<String> line = RPGCore.inst().getLanguageManager().getTranslationList(this.name);
        return line.size() < 2 ? Collections.emptyList() : line.subList(2, line.size());
    }

    /**
     * Used to identify skills using certain keywords.
     *
     * @return tags identifying the skills.
     */
    public List<String> getTags() {
        return tags;
    }
}