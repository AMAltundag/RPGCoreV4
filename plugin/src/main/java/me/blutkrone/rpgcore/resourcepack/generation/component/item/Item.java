package me.blutkrone.rpgcore.resourcepack.generation.component.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.ConfiguredPredicate;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.CustomModelPredicate;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.IOverridePredicate;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.TrimPredicate;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An item in the resourcepack, which contains overrides that will
 * show a custom item.
 */
public class Item {

    // inherit no model data from parent
    public static String PARENT_GENERATED = "minecraft:item/generated";
    // inherit basic properties of being able to 'hold' the item
    public static String PARENT_HANDHELD = "minecraft:item/handheld";

    // material we are representing
    public final Material material;
    // custom models established via predicate
    public final List<ItemOverride> overrides;
    // base layout for the JSON configuration
    public final JsonObject template;

    /**
     * An item in the resourcepack, which contains overrides that will
     * show a custom item.
     *
     * @param material
     * @param parent
     */
    public Item(Material material, String parent) {
        this.material = material;
        this.template = new JsonObject();
        this.template.addProperty("parent", parent);
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/" + material.name().toLowerCase());
        this.template.add("textures", textures);

        this.overrides = new ArrayList<>();
    }

    /**
     * An item in the resourcepack, which contains overrides that will
     * show a custom item.
     *
     * @param material
     * @param template
     */
    public Item(Material material, JsonObject template) {
        this.material = material;
        this.template = template;
        this.overrides = new ArrayList<>();

        JsonArray overrides = (JsonArray) template.get("overrides");
        if (overrides != null) {
            for (JsonElement override : overrides) {
                if (override instanceof JsonObject o) {
                    String model = o.get("model").getAsString();
                    IOverridePredicate predicate = new ConfiguredPredicate((JsonObject) o.get("predicate"));
                    this.overrides.add(new ItemOverride(predicate, model));
                }
            }
        }
    }

    /**
     * Add an override to the item.
     *
     * @param predicate When to use the model
     * @param model The model to use
     */
    public void add(IOverridePredicate predicate, String model) {
        this.overrides.add(new ItemOverride(predicate, model));
    }

    /**
     * Transform this item configuration into the JSON format.
     *
     * @return This item in JSON
     */
    public JsonObject export() {

        JsonObject object = this.template.deepCopy();
        JsonArray overrides = new JsonArray();
        for (ItemOverride override : this.overrides) {
            overrides.add(override.export());
        }
        object.add("overrides", overrides);
        return object;
    }

    public class ItemOverride {
        // when does the override apply
        public final IOverridePredicate predicate;
        // the model to use during override
        public final String model;

        public ItemOverride(IOverridePredicate predicate, String model) {
            this.predicate = predicate;
            this.model = model;
        }

        public JsonObject export() {
            JsonObject object = new JsonObject();
            object.add("predicate", this.predicate.export());
            object.addProperty("model", this.model);
            return object;
        }

        public static List<ItemOverride> sort(List<ItemOverride> overrides) {
            List<ItemOverride> trims = new ArrayList<>();
            List<ItemOverride> models = new ArrayList<>();
            List<ItemOverride> configs = new ArrayList<>();

            for (ItemOverride override : overrides) {
                if (override.predicate instanceof ConfiguredPredicate) {
                    configs.add(override);
                } else if (override.predicate instanceof TrimPredicate) {
                    trims.add(override);
                } else if (override.predicate instanceof CustomModelPredicate) {
                    models.add(override);
                }
            }

            // sort overrides by model data
            models.sort(Comparator.comparingInt(override -> {
                return ((CustomModelPredicate) override.predicate).getCustomModelData();
            }));

            // join back into one list
            List<ItemOverride> sorted = new ArrayList<>();
            sorted.addAll(configs);
            sorted.addAll(trims);
            sorted.addAll(models);

            return sorted;
        }
    }
}
