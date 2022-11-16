package me.blutkrone.rpgcore.skill;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.attribute.AttributeCollection;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.Location;

import java.util.*;

/**
 * A base interface which is to be implemented by any
 * source which can invoke a skill.
 */
public class SkillContext implements IContext, IOrigin {

    // id of the skill creating context
    private String skillId;
    // who was this context created for
    private CoreEntity owner;
    // attributes tied into this context
    private Map<String, AttributeCollection> attribute = new HashMap<>();
    // tags tied into this context
    private Set<String> tags = new HashSet<>();
    // skills linked to the context
    private List<String> linked = new ArrayList<>();

    /**
     * A context dedicated for the invocation of a skill.
     *
     * @param owner who owns the context.
     */
    public SkillContext(CoreEntity owner, CoreSkill skill) {
        this.owner = owner;
        this.skillId = skill.getId();
    }

    /**
     * The skill backing up the context.
     *
     * @return the skill of the context.
     */
    public CoreSkill getSkill() {
        return RPGCore.inst().getSkillManager().getIndex().get(this.skillId);
    }

    /**
     * Have this context acquire a tag.
     *
     * @param tag the tag to be acquired.
     */
    public void addTag(String tag) {
        this.tags.add(tag);
    }

    /**
     * Other skills which we have a link to, this is intended
     * for the "trigger" mechanics to invoke another skill.
     * <p>
     * Triggered skills will inherit this skill context, however
     * without any linked skills.
     *
     * @return linked skills.
     */
    public List<String> getLinkedSkills() {
        return linked;
    }

    /**
     * Attribute collection at a local scope, this will be summed with
     * the entity that instantiated the context.
     *
     * @return the attribute collection
     */
    public AttributeCollection getAttributeLocal(String attribute) {
        return this.attribute.computeIfAbsent(attribute, (k -> new AttributeCollection(IContext.EMPTY)));
    }

    @Override
    public double evaluateAttribute(String attribute) {
        return this.owner.evaluateAttribute(attribute)
                + this.attribute.computeIfAbsent(attribute, (k -> new AttributeCollection(IContext.EMPTY))).evaluate();
    }

    @Override
    public boolean checkForTag(String tag) {
        return this.tags.contains(tag) || this.owner.checkForTag(tag);
    }

    @Override
    public CoreEntity getCoreEntity() {
        return this.owner;
    }

    @Override
    public Location getLocation() {
        return this.owner.getLocation();
    }
}
