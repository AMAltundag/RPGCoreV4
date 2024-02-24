package me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate;

import com.google.gson.JsonObject;

public class TrimPredicate implements IOverridePredicate {
    private final double trim_type;

    /**
     * Predicate to display via trim type.
     *
     * @param trim_type Required trim type..
     */
    public TrimPredicate(double trim_type) {
        this.trim_type = trim_type;
    }

    @Override
    public JsonObject export() {
        JsonObject output = new JsonObject();
        output.addProperty("trim_type", this.trim_type);
        return output;
    }
}
