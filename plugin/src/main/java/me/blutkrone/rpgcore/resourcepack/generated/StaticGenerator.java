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

public class StaticGenerator {

    public static Map<String, IndexedTexture> construct(File file, String table, String suffix, int offset, PooledSymbolSpace symbol) {
        Map<String, IndexedTexture> textures = new HashMap<>();
        try {
            BufferedImage bi = ImageIO.read(file);
            List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();

            // slice the menu texture appropriately
            int current = 0;
            while (current < bi.getWidth()) {
                // slice up the texture and pool it
                BufferedImage slice = bi.getSubimage(current, 0, Math.min(128, bi.getWidth() - current), bi.getHeight());
                sliced.add(new IndexedTexture.GeneratedTexture(symbol.current++, table, slice.getWidth(), slice, offset));
                // move our pointer ahead
                current += Math.min(128, bi.getWidth());
            }
            String name = file.getName();
            name = name.substring(0, name.indexOf("."));
            if (!suffix.isEmpty()) name += "_" + suffix;
            textures.put("static_" + name, IndexedTexture.GeneratedCompoundTexture.build(sliced));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textures;
    }

    public static Map<String, IndexedTexture> construct(File file, String table, int offset, PooledSymbolSpace symbol) {
        return construct(file, table, "", offset, symbol);
    }

    public static class PooledSymbolSpace {
        public char current = 0xff;
    }
}
