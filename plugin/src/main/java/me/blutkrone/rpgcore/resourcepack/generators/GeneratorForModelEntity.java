package me.blutkrone.rpgcore.resourcepack.generators;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Model;
import me.blutkrone.rpgcore.bbmodel.io.serialized.geometry.BBGeometry;
import me.blutkrone.rpgcore.bbmodel.io.serialized.BBModel;
import me.blutkrone.rpgcore.bbmodel.io.serialized.BBTexture;
import me.blutkrone.rpgcore.bbmodel.io.serialized.purpose.BedrockEntityModel;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.CustomModelPredicate;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GeneratorForModelEntity implements IGenerator {
    private static final File OUTPUT_MODEL = FileUtil.directory("resourcepack/working/assets/minecraft/models/generated");
    private static final File OUTPUT_TEXTURE = FileUtil.directory("resourcepack/working/assets/minecraft/textures/generated");

    private static final File INPUT_ENTITY2 = new File(RPGCore.inst().getDataFolder().getParentFile() + File.separator + "__rpgcore" + File.separator + "entity_bbmodel");
    private static final File INPUT_ENTITY = FileUtil.directory("resourcepack/input/bb_entity");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        // process models from blockbench
        for (File file_bbmodel : FileUtil.buildAllFiles(INPUT_ENTITY, INPUT_ENTITY2)) {
            // identify which item backs up the given model
            String file_name = file_bbmodel.getName();
            if (file_name.endsWith(".bbmodel")) {
                final String model_name = file_name.replace(".bbmodel", "");
                BBModel model = new BedrockEntityModel(file_bbmodel, BBUtil.fileToJson(file_bbmodel));

                // dump the shape model
                for (BBGeometry geometry : model.geometry) {
                    JsonObject exported_model = geometry.exportLocal(model, file_bbmodel);

                    if (exported_model != null) {
                        try {
                            // unique ID for the bone
                            final String json_id = "generated/bbmodel/entity/" + model_name + "/" + geometry.name;
                            final String file_id = "bbmodel" + File.separator + "entity" + File.separator + model_name + File.separator + geometry.name;

                            // assign the generated item to the bone
                            geometry.render_data = generation.model().getNextCustomModelData(Material.LEATHER_HORSE_ARMOR);

                            // make the generated item accessible
                            generation.model().register(Material.LEATHER_HORSE_ARMOR)
                                    .add(new CustomModelPredicate(geometry.render_data), json_id);

                            // store the model on the disk
                            generation.write(new File(OUTPUT_MODEL + File.separator + file_id + ".json"), exported_model);
                        } catch (IOException e) {
                            Bukkit.getLogger().severe("Could not save bone %s from %s".formatted(geometry, file_bbmodel.getPath()));
                        }
                    }
                }

                // dump the backing textures
                for (BBTexture bb_texture : model.textures) {
                    // dump the actual texture
                    String texture_path = "bbtexture_" + bb_texture.uuid + ".png";
                    File dump_texture_at = FileUtil.file(OUTPUT_TEXTURE, texture_path);
                    ImageIO.write(bb_texture.image, "png", dump_texture_at);

                    // dump the optional mcmeta
                    String mcmeta_path = texture_path + ".mcmeta";
                    JsonObject mcmeta_data = bb_texture.mcmeta;
                    if (mcmeta_data != null) {
                        File mcmeta_file = new File(OUTPUT_TEXTURE, mcmeta_path);
                        generation.write(mcmeta_file, mcmeta_data);
                    }
                }

                // dump minimum information to animate entity
                generation.model().register(model_name, new Model(model_name, model));
            }
        }
    }
}
