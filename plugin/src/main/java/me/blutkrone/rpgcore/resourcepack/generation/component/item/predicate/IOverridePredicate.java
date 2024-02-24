package me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate;

import com.google.gson.JsonObject;

public interface IOverridePredicate {
    /**
     * Export the predicate into a JSON object that can be
     * embedded into the resourcepack.
     *
     * @return JSON format of this predicate.
     */
    JsonObject export();
}
