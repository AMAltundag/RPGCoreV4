package me.blutkrone.rpgcore.api;

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
}
