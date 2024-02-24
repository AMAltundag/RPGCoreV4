package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom armor textures
 * @see <a href="https://github.com/Ancientkingg/fancyPants/blob/master/README.md">Dependent on a coreshader</a>
 * @see <a href="https://github.com/Godlander/lessfancypants">Update to lessfancypants for better performance</a>
 */
public class GeneratorForArmor implements IGenerator {
    private static final File OUTPUT_ARMOR = FileUtil.directory("resourcepack/working/assets/minecraft/textures/models/armor");
    private static final File INPUT_ARMOR = FileUtil.directory("resourcepack/input/armor");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Bukkit.getLogger().severe("Not implemented (Replace FancyPants with custom trims)");

        // each shader armor has two layers
        Map<Integer, BufferedImage> layer1 = new HashMap<>();
        Map<Integer, BufferedImage> layer2 = new HashMap<>();

        // load all shader armors on the disk
        for (File armor_files : INPUT_ARMOR.listFiles()) {
            int color = Integer.parseInt(armor_files.getName());
            if (color >= (2 << (8 * 3 - 1)) || color < 0) {
                RPGCore.inst().getLogger().severe("Illegal Shader Armor ID: " + color);
            } else {
                layer1.put(color, ImageIO.read(new File(armor_files, "layer_1.png")));
                layer2.put(color, ImageIO.read(new File(armor_files, "layer_2.png")));
            }
        }
        int max_height = 0;
        for (BufferedImage image : layer1.values())
            max_height = Math.max(max_height, image.getHeight());
        for (BufferedImage image : layer2.values())
            max_height = Math.max(max_height, image.getHeight());

        // ensure file-path exists
        OUTPUT_ARMOR.mkdirs();

        // create empty overlay textures
        ImageIO.write(new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB), "png", new File(OUTPUT_ARMOR, "leather_layer_1_overlay.png"));
        ImageIO.write(new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB), "png", new File(OUTPUT_ARMOR, "leather_layer_2_overlay.png"));

        // concat all other textures
        BufferedImage joined1 = new BufferedImage(64 * layer1.size(), max_height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage joined2 = new BufferedImage(64 * layer2.size(), max_height, BufferedImage.TYPE_INT_ARGB);

        // armor 0 is the default leather armor
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 32; j++) {
                joined1.setRGB(i, j, layer1.get(0).getRGB(i, j));
                joined2.setRGB(i, j, layer2.get(0).getRGB(i, j));
            }
        }

        // drop the layer0 from further processing
        layer1.remove(0);
        layer2.remove(0);

        // everything else should simply be appended
        int n = 1;
        for (Map.Entry<Integer, BufferedImage> layer1_entry : layer1.entrySet()) {
            // fetch the texture which we are operating with
            BufferedImage layer1_texture = layer1_entry.getValue();
            BufferedImage layer2_texture = layer2.get(layer1_entry.getKey());
            // draw the respective textures
            for (int i = 0; i < 64; i++) {
                for (int j = 0; j < Math.min(layer1_texture.getHeight(), layer2_texture.getHeight()); j++) {
                    joined1.setRGB(i + 64 * n, j, layer1_texture.getRGB(i, j));
                    joined2.setRGB(i + 64 * n, j, layer2_texture.getRGB(i, j));
                }
            }
            // draw the identifying marker
            int marker = layer1_entry.getKey() | 0xFF000000;
            joined1.setRGB(64 * n, 0, marker);
            joined2.setRGB(64 * n, 0, marker);
            // increment our counter
            n += 1;
        }

        // save the joined layers on the disk
        ImageIO.write(joined1, "png", new File(OUTPUT_ARMOR, "leather_layer_1.png"));
        ImageIO.write(joined2, "png", new File(OUTPUT_ARMOR, "leather_layer_2.png"));
    }
}
