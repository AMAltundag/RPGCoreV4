package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationGenerator {

    private static final int MENU_VERTICAL_OFFSET = 14 + 8;

    public static Map<String, IndexedTexture> createSlotAnimation(File directory) {
        Map<String, IndexedTexture> textures = new HashMap<>();
        try {
            for (File folder : directory.listFiles()) {
                char c = 0xff;
                if (folder.isDirectory()) {
                    String key = "animation_slot_" + folder.getName();

                    for (File file : folder.listFiles()) {
                        if (!file.getName().endsWith(".png"))
                            continue;
                        // dupe the animation textures
                        for (int i = 0; i < 6; i++) {
                            BufferedImage bi = ImageIO.read(file);
                            List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();
                            // slice the menu texture appropriately
                            int w = 0;
                            while (w < bi.getWidth()) {
                                // slice up the texture and pool it
                                BufferedImage slice = bi.getSubimage(w, 0, Math.min(128, bi.getWidth() - w), bi.getHeight());
                                sliced.add(new IndexedTexture.GeneratedTexture(c++, key, slice.getWidth(), slice, MENU_VERTICAL_OFFSET - (18 * i) - 3));
                                // move our pointer ahead
                                w += Math.min(128, bi.getWidth());
                            }
                            // identify the base path of the animation
                            String id = key + "_" + i + "_" + file.getName();
                            id = id.substring(0, id.indexOf("."));
                            // provide this symbol as our result
                            textures.put(id, IndexedTexture.GeneratedCompoundTexture.build(sliced));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }
}
