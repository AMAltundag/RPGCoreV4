package me.blutkrone.rpgcore.attribute;

import me.blutkrone.rpgcore.editor.root.other.EditorAttribute;

import java.util.List;
import java.util.stream.Collectors;

public class CoreAttribute {

    // unique identifier for the attribute
    private String id;
    // default value of the attribute
    private double defaults;
    // default inheritance from other attributes
    private List<Inherited> inherited;
    // deprecated attributes should be avoided
    private boolean deprecated;
    // force multiplier groups

    public CoreAttribute(String id, EditorAttribute editor) {
        this.id = id;
        this.deprecated = editor.deprecated;
        this.defaults = editor.defaults;
        this.inherited = editor.inherited
                .stream()
                .map(i -> new Inherited(i.source, i.multiplier))
                .collect(Collectors.toList());
    }

    /**
     * A default value is acquired when the attribute collection is
     * created, it will not be updated afterwards.
     *
     * @return default value of this attribute
     */
    public double getDefaults() {
        return defaults;
    }

    /**
     * When deprecated, editors will reduce visibility for this
     * attribute and offer a warning if referenced. However the
     * attribute is fully functional.
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Provide the basic setup of the given attribute, which
     * will contain modifiers for this attribute specifically
     *
     * @param collection what collection to update
     */
    public void setup(AttributeCollection collection) {
        // offer the default constant of the attribute
        collection.create(this.defaults);
        // offer the inherited modifiers of the attribute
        for (Inherited inheritance : this.inherited)
            collection.create(inheritance.source, inheritance.multiplier);
    }

    /**
     * A unique identifier for this attribute.
     *
     * @return unique attribute identifier.
     */
    public String getId() {
        return id;
    }

    /*
     * Construct to inherit modifiers from other attributes.
     */
    private final class Inherited {
        private final String source;
        private final String multiplier;

        private Inherited(String source, String multiplier) {
            this.source = source;
            this.multiplier = multiplier;
        }
    }
}