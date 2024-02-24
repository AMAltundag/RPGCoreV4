package me.blutkrone.rpgcore.resourcepack.generators;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.io.serialized.BBModel;
import me.blutkrone.rpgcore.bbmodel.io.serialized.BBTexture;
import me.blutkrone.rpgcore.bbmodel.io.serialized.purpose.JavaItemModel;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.Item;
import me.blutkrone.rpgcore.resourcepack.generation.component.item.predicate.CustomModelPredicate;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.io.File;

public class GeneratorForModelDecoration implements IGenerator {

    private static final File OUTPUT_MODEL = FileUtil.directory("resourcepack/working/assets/minecraft/models/generated");
    private static final File OUTPUT_TEXTURE = FileUtil.directory("resourcepack/working/assets/minecraft/textures/generated");

    private static final File INPUT_DECORATION2 = new File(RPGCore.inst().getDataFolder().getParentFile() + File.separator + "__rpgcore" + File.separator + "decoration_bbmodel");
    private static final File INPUT_DECORATION = FileUtil.directory("resourcepack/input/bb_decoration");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        // process models from blockbench
        for (File file_bbmodel : FileUtil.buildAllFiles(INPUT_DECORATION, INPUT_DECORATION2)) {
            String file_name = file_bbmodel.getName();
            if (file_name.endsWith(".bbmodel")) {
                String model_name = file_name.replace(".bbmodel", "");
                BBModel model = new JavaItemModel(file_bbmodel, BBUtil.fileToJson(file_bbmodel));

                final String json_id = "generated/bbmodel/decoration/" + model_name;
                final String file_id = "bbmodel" + File.separator + "decoration" + File.separator + model_name;

                JsonObject exported_model = model.geometry.exportGlobal(model, file_bbmodel);
                if (exported_model != null) {
                    // create a model for the decoration
                    generation.write(new File(OUTPUT_MODEL + File.separator + file_id + ".json"), exported_model);

                    // register model as an appropriate item
                    try {
                        int split_point = model_name.lastIndexOf('_');
                        Material material = Material.valueOf(model_name.substring(0, split_point).toUpperCase());
                        int model_data = Integer.parseInt(model_name.substring(split_point + 1));

                        Item item = generation.model().register(material);
                        item.add(new CustomModelPredicate(model_data), json_id);
                    } catch (Exception ex) {
                        Bukkit.getLogger().warning("Could not register: " + file_bbmodel.getPath());
                        continue;
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
                }
            }
        }
    }
}
