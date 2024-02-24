package me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate;

import com.google.gson.JsonObject;

public class CustomModelPredicate implements IOverridePredicate {

    private final int custom_model_data;

    /**
     * Predicate to display via custom model data.
     *
     * @param custom_model_data Required model data.
     */
    public CustomModelPredicate(int custom_model_data) {
        this.custom_model_data = custom_model_data;
    }

    public int getCustomModelData() {
        return custom_model_data;
    }

    @Override
    public JsonObject export() {
        JsonObject output = new JsonObject();
        output.addProperty("custom_model_data", this.custom_model_data);
        return output;
    }
}
