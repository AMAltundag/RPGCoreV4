package me.blutkrone.rpgcore.attribute;

import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorAttribute;

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
    }

    /**
     * The index which tracks our attributes.
     *
     * @return the index
     */
    public EditorIndex<CoreAttribute, EditorAttribute> getIndex() {
        return index;
    }
}