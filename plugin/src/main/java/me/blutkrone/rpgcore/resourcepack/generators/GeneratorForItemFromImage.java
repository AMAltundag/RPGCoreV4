package me.blutkrone.rpgcore.resourcepack.generators;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.Item;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.CustomModelPredicate;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;

import java.io.File;
import java.util.UUID;

public class GeneratorForItemFromImage implements IGenerator {

    private static final File INPUT_ITEM_FROM_IMAGE = FileUtil.directory("resourcepack/input/item");

    private static final File WORKSPACE_TEXTURE = FileUtil.directory("resourcepack/working/assets/minecraft/textures/generated");
    private static final File WORKSPACE_MODELS_GENERATED = FileUtil.directory("resourcepack/working/assets/minecraft/models/generated");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        for (File file_custom_texture : FileUtil.buildAllFiles(INPUT_ITEM_FROM_IMAGE)) {
            String file_name = file_custom_texture.getName();
            if (file_name.endsWith(".png")) {
                file_name = file_name.replace(".png", "");
                int split_point = file_name.lastIndexOf('_');
                if (split_point == -1) {
                    continue;
                }

                // identify which item we want to model over
                Material material = Material.valueOf(file_name.substring(0, split_point).toUpperCase());
                int model_data = Integer.parseInt(file_name.substring(split_point + 1));
                // dump the texture on-disk
                String unique_id = "item_" + UUID.randomUUID();
                FileUtils.copyFile(file_custom_texture, FileUtil.file(WORKSPACE_TEXTURE, unique_id + ".png"));
                // register an override for this model
                Item item = generation.model().register(material);
                item.add(new CustomModelPredicate(model_data), "minecraft:generated/" + unique_id);
                // dump a model override for the item
                JsonObject personal_model = new JsonObject();
                personal_model.addProperty("parent", Item.PARENT_HANDHELD);
                JsonObject textures = new JsonObject();
                textures.addProperty("layer0", "minecraft:generated/" + unique_id);
                personal_model.add("textures", textures);

                generation.write(new File(WORKSPACE_MODELS_GENERATED, unique_id + ".json"), personal_model);
            }
        }
    }
}
