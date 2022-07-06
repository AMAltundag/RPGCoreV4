package me.blutkrone.rpgcore.resourcepack.component;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class ResourcePackItem {
    public final String parent;
    public final Map<String, String> textures;
    public final List<ItemOverride> overrides = new ArrayList<>();

    public ResourcePackItem(String parent, Map<String, String> textures) {
        this.parent = parent;
        this.textures = textures;
    }

    public JSONObject transform() {
        JSONObject object = new JSONObject();
        object.put("parent", this.parent);
        JSONObject container = new JSONObject();
        textures.forEach((key, value) -> {
            container.put(key, value);
        });
        object.put("textures", container);
        JSONArray array = new JSONArray();
        overrides.sort(Comparator.comparingInt(o -> o.custom_model_data));
        for (ItemOverride override : overrides)
            array.add(override.transform());
        object.put("overrides", array);
        return object;
    }

    public static class ItemModel {
        public final String parent;
        public final Map<String, String> textures;

        public ItemModel(String parent, String texture) {
            this.parent = parent;
            this.textures = new HashMap<>();
            this.textures.put("layer0", texture);
        }

        public ItemModel(String parent, Map<String, String> textures) {
            this.parent = parent;
            this.textures = textures;
        }

        public JSONObject transform() {
            JSONObject object = new JSONObject();
            object.put("parent", this.parent);
            JSONObject container = new JSONObject();
            textures.forEach((key, value) -> {
                container.put(key, value);
            });
            object.put("textures", container);
            return object;
        }
    }

    public static class ItemOverride {
        public final int custom_model_data;
        public final String model;

        public ItemOverride(int custom_model_data, String model) {
            this.custom_model_data = custom_model_data;
            this.model = model;
        }

        public JSONObject transform() {
            JSONObject predicate = new JSONObject();
            predicate.put("custom_model_data", custom_model_data);

            JSONObject object = new JSONObject();
            object.put("model", model);
            object.put("predicate", predicate);
            return object;
        }
    }
}
