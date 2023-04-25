package me.blutkrone.rpgcore.api;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import me.blutkrone.rpgcore.skill.proxy.AbstractSkillProxy;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * A context is used across various places, serving as a uniform
 * interface to provide limited logic. Usage includes, but isn't
 * limited to:
 * <ul>
 * <li>Players and monsters</li>
 * <li>Proxies created by skills</li>
 * </ul>
 */
public interface IContext {

    // a context without any data persistence on it
    IContext EMPTY = new IContext() {
        @Override
        public double evaluateAttribute(String attribute) {
            return 0;
        }

        @Override
        public boolean checkForTag(String tag) {
            return false;
        }

        @Override
        public CoreEntity getCoreEntity() {
            return null;
        }

        @Override
        public IOrigin getOrigin() {
            return null;
        }

        @Override
        public void addProxy(AbstractSkillProxy proxy) {
            Bukkit.getLogger().severe("Cannot add proxy to empty context");
        }

        @Override
        public List<AbstractSkillProxy> getProxies() {
            return new ArrayList<>();
        }

        @Override
        public void addPipeline(CoreAction.ActionPipeline pipeline) {
            Bukkit.getLogger().severe("Cannot add pipeline to empty context");
        }

        @Override
        public List<CoreAction.ActionPipeline> getPipelines() {
            return null;
        }
    };

    /**
     * Evaluate the value of an attribute, relative to what sort of
     * type it has.
     *
     * @param attribute which attribute to look up.
     * @return the value of the given attribute
     */
    double evaluateAttribute(String attribute);

    /**
     * Check if a certain tag is present or not.
     *
     * @param tag the tag to check for.
     * @return whether we have the tag or not.
     */
    boolean checkForTag(String tag);

    /**
     * Retrieve the entity which is associated with the context.
     *
     * @return who is the context related to.
     */
    CoreEntity getCoreEntity();

    /**
     * The origin of the context, if core entity exists
     * it should match with this.
     *
     * @return Origin of context.
     */
    IOrigin getOrigin();

    /**
     * Register a proxy to the backing object.
     *
     * @param proxy Proxy
     */
    void addProxy(AbstractSkillProxy proxy);

    /**
     * Retrieve a list of all proxies registered.
     *
     * @return Registered proxies.
     */
    List<AbstractSkillProxy> getProxies();

    /**
     * Register an action pipeline to the backing object.
     *
     * @param pipeline Pipeline
     */
    void addPipeline(CoreAction.ActionPipeline pipeline);

    /**
     * Retrieve a list of all pipelines registered.
     *
     * @return Registered pipelines.
     */
    List<CoreAction.ActionPipeline> getPipelines();
}
