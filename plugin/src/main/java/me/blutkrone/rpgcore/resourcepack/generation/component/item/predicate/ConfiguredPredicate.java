package me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate;

import com.google.gson.JsonObject;

public class ConfiguredPredicate implements IOverridePredicate {

    private final JsonObject serialized;

    /**
     * Predicate that is inherited from a JSON configuration.
     *
     * @param serialized Configuration
     */
    public ConfiguredPredicate(JsonObject serialized) {
        this.serialized = serialized;
    }

    @Override
    public JsonObject export() {
        return this.serialized;
    }
}
