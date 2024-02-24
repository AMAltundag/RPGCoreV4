package me.blutkrone.rpgcore.bbmodel.io.serialized;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.io.serialized.animation.BBAnimation;
import me.blutkrone.rpgcore.bbmodel.io.serialized.geometry.BBCube;
import me.blutkrone.rpgcore.bbmodel.io.serialized.geometry.BBGeometry;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.Item;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BBModel {

    // file we are serialized from
    public File bb_file;
    // all cubes to be treated as a hitbox
    public List<BoundingBox> hitboxes;
    // geometry of the model
    public BBGeometry geometry;
    // JSON template used by all geometry
    public JsonObject template;
    // animations the model is capable of
    public Map<String, BBAnimation> animations;
    // textures encoded into the model
    public List<BBTexture> textures;

    /**
     * A wrapper that allows us to translate bbmodel files into
     * something that minecraft java can work with.
     *
     * @param bb_file The *.bbmodel file we want to translate
     * @param bb_model The JSON representation of the file
     * @throws IOException Should we fail to parse the files.
     */
    public BBModel(File bb_file, JsonObject bb_model) throws IOException {
        // load the respective parts
        loadGeneral(bb_model, bb_file);
        loadGeometry(bb_model, bb_file);
        loadTexture(bb_model, bb_file);
        loadAnimation(bb_model, bb_file);
        loadHitbox(bb_model, bb_file);
    }

    private void loadGeneral(JsonObject bb_model, File bb_file) throws IOException {
        this.template = new JsonObject();

        // initialize default parent for model
        this.template.addProperty("parent", Item.PARENT_GENERATED);

        // inherit configurations from parent
        BBUtil.transfer(bb_model, this.template, "parent");
        BBUtil.transfer(bb_model, this.template, "ambientocclusion");
        BBUtil.transfer(bb_model, this.template, "display");

        // create an appropriate display section
        JsonObject display;
        if (!this.template.has("display")) {
            this.template.add("display", new JsonObject());
        }
        display = this.template.getAsJsonObject("display");

        // item display uses type 'fixed'
        if (!display.has("fixed")) {
            BBDisplay bb_display = new BBDisplay();
            bb_display.scale(1f, 1f, 1f);
            display.add("fixed", bb_display.export());
        }
    }

    private void loadGeometry(JsonObject bb_model, File bb_file) throws IOException {
        Map<UUID, BBCube> cubes = new HashMap<>();

        // extract the cubes making up the geometry
        JsonArray bb_elements = (JsonArray) bb_model.get("elements");
        if (bb_elements != null) {
            for (JsonElement bb_element : bb_elements) {
                BBCube cube = new BBCube(((JsonObject) bb_element));
                cubes.put(cube.uuid, cube);
            }
        }

        // parse outliner into geometry hierarchy
        JsonArray bb_outliner = (JsonArray) bb_model.get("outliner");
        if (bb_outliner != null) {
            this.geometry = new BBGeometry(bb_outliner, cubes);
        } else {
            this.geometry = new BBGeometry();
            this.geometry.elements.addAll(cubes.values());
        }
    }

    private void loadHitbox(JsonObject bb_model, File bb_file) {
        this.hitboxes = new ArrayList<>();
        JsonArray bb_outliner = (JsonArray) bb_model.get("outliner");

        // scan for all cubes representing the hitbox
        Set<UUID> hitbox_cubes = new HashSet<>();
        for (JsonElement bb_outline : bb_outliner) {
            if (bb_outline instanceof JsonObject bb_obj) {
                if ("hitbox".equals(bb_obj.get("name").getAsString())) {
                    for (JsonElement bb_child : bb_obj.getAsJsonArray("children")) {
                        if (bb_child.isJsonPrimitive()) {
                            hitbox_cubes.add(UUID.fromString(bb_child.getAsString()));
                        }
                    }
                }
            }
        }

        // match with the cubes
        JsonArray bb_elements = (JsonArray) bb_model.get("elements");
        if (bb_elements != null) {
            for (JsonElement bb_element : bb_elements) {
                BBCube cube = new BBCube(((JsonObject) bb_element));
                if (hitbox_cubes.contains(cube.uuid)) {
                    float[] from = cube.from;
                    float[] to = cube.to;
                    BoundingBox hitbox = new BoundingBox(from[0], from[1], from[2], to[0], to[1], to[2]);
                    hitboxes.add(hitbox);
                }
            }
        }
    }

    private void loadTexture(JsonObject bb_model, File bb_file) throws IOException {
        this.textures = new ArrayList<>();

        // load our compounded mcmeta for textures
        JsonObject mcmeta = null;
        File mcmeta_file = new File(bb_file.getAbsolutePath() + File.separator + ".mcmeta");
        if (mcmeta_file.exists()) {
            try (FileReader reader = new FileReader(mcmeta_file)) {
                mcmeta = RPGCore.inst().getGsonUgly().fromJson(reader, JsonObject.class);
            }
        }

        // load the textures encoded into the bbmodel file
        JsonArray bb_textures = (JsonArray) bb_model.get("textures");
        if (bb_textures != null) {
            JsonObject texture_section = new JsonObject();
            for (JsonElement bb_texture : bb_textures) {
                BBTexture texture = new BBTexture(bb_texture.getAsJsonObject(), mcmeta);
                texture_section.addProperty(texture.id, "generated/bbtexture_" + texture.uuid);
                textures.add(texture);
            }
            this.template.add("textures", texture_section);
        }
    }

    private void loadAnimation(JsonObject bb_model, File bb_file) throws IOException {
        this.animations = new HashMap<>();

        JsonArray bb_animations = bb_model.getAsJsonArray("animations");
        if (bb_animations != null) {
            for (JsonElement bb_animation : bb_animations) {
                BBAnimation animation = new BBAnimation((JsonObject) bb_animation);
                this.animations.put(animation.name, animation);
            }
        }
    }
}
