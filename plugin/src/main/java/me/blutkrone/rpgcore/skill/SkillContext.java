package me.blutkrone.rpgcore.skill;

import me.blutkrone.rpgcore.api.IContext;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.attribute.AttributeCollection;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A base interface which is to be implemented by any
 * source which can invoke a skill.
 */
public class SkillContext implements IContext, IOrigin {

    // who was this context created for
    private CoreEntity owner;
    // attributes tied into this context
    private Map<String, AttributeCollection> attribute = new HashMap<>();
    // tags tied into this context
    private Set<String> tags = new HashSet<>();

    /**
     * A context dedicated for the invocation of a skill.
     *
     * @param owner who owns the context.
     */
    public SkillContext(CoreEntity owner) {
        this.owner = owner;
    }

    /**
     * Have this context acquire a tag.
     *
     * @param tag the tag to be acquired.
     */
    public void addTag(String tag) {
        this.tags.add(tag);
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
