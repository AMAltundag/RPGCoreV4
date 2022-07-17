package me.blutkrone.rpgcore.resourcepack.generated;

import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.resourcepack.utils.ResourcepackGeneratorMeasured;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoreGenerator {

    public static Map<String, IndexedTexture> construct(File style, File icons, File jewels, ResourcepackGeneratorMeasured rules, StaticGenerator.PooledSymbolSpace symbol) {
        Map<String, IndexedTexture> textures = new HashMap<>();

        try {
            for (File style_folder : style.listFiles()) {
                String id = style_folder.getName();

                // load the basic textures for the lore
                textures.putAll(build(ImageIO.read(new File(style_folder, "body_separator.png")), symbol, "lore_body_separator_" + id));
                textures.putAll(build(ImageIO.read(new File(style_folder, "body_bottom.png")), symbol, "lore_body_bottom_" + id));
                textures.putAll(build(ImageIO.read(new File(style_folder, "body_top.png")), symbol, "lore_body_top_" + id));
                textures.putAll(build(ImageIO.read(new File(style_folder, "footer.png")), symbol, "lore_footer_" + id));
                textures.putAll(build(ImageIO.read(new File(style_folder, "header.png")), symbol, "lore_header_" + id));
                textures.putAll(build(ImageIO.read(new File(style_folder, "highlight_top.png")), symbol, "lore_highlight_top_" + id));
                textures.putAll(build(ImageIO.read(new File(style_folder, "highlight_bottom.png")), symbol, "lore_highlight_bottom_" + id));
                textures.putAll(build(ImageIO.read(new File(style_folder, "highlight_separator.png")), symbol, "lore_highlight_separator_" + id));
                // load up title texture
                textures.putAll(build(ImageIO.read(new File(style_folder, "title.png")), symbol, "lore_title_" + id));
                // special offsets to cover hiding the original lore
                for (IndexedTexture.GeneratedTexture texture : ((IndexedTexture.GeneratedCompoundTexture) textures.get("lore_header_" + id + "_0")).getTextures()) {
                    texture.offset += 20;
                }
                for (IndexedTexture.GeneratedTexture texture : ((IndexedTexture.GeneratedCompoundTexture) textures.get("lore_header_" + id + "_1")).getTextures()) {
                    texture.offset += 10;
                }
                for (IndexedTexture.GeneratedTexture texture : ((IndexedTexture.GeneratedCompoundTexture) textures.get("lore_footer_" + id + "_1")).getTextures()) {
                    texture.offset -= 10;
                }
            }

            // load up textures for icons shown in lore
            for (File file : FileUtil.buildAllFiles(icons)) {
                String name = file.getName().replace(".png", "");
                textures.put("lore_icon_" + name, build(ImageIO.read(file), symbol));
            }

            // load up textures for icons shown in lore
            for (File file : FileUtil.buildAllFiles(jewels)) {
                String name = file.getName().replace(".png", "");
                textures.put("lore_jewel_" + name, build(ImageIO.read(file), symbol));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return textures;
    }

    private static IndexedTexture build(BufferedImage bi, StaticGenerator.PooledSymbolSpace symbol) {
        // slice up horizontally as deemed appropriate
        List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();
        // slice the menu texture appropriately
        int current = 0;
        while (current < bi.getWidth()) {
            // slice up the texture and pool it
            BufferedImage slice = bi.getSubimage(current, 0, Math.min(128, bi.getWidth() - current), bi.getHeight());
            sliced.add(new IndexedTexture.GeneratedTexture(symbol.current++, "lore_design", slice.getWidth(), slice, bi.getHeight()));
            // move our pointer ahead
            current += Math.min(128, bi.getWidth());
        }
        // offer up the texture we generated
        return IndexedTexture.GeneratedCompoundTexture.build(sliced);
    }

    private static Map<String, IndexedTexture> build(BufferedImage raw, StaticGenerator.PooledSymbolSpace symbol, String prefix) {
        Map<String, IndexedTexture> output = new HashMap<>();

        for (int i = 0; i < raw.getHeight(); i += 10) {
            // generate vertical slice
            BufferedImage bi = raw.getSubimage(0, i, raw.getWidth(), 10);
            // slice up horizontally as deemed appropriate
            List<IndexedTexture.GeneratedTexture> sliced = new ArrayList<>();
            // slice the menu texture appropriately
            int current = 0;
            while (current < bi.getWidth()) {
                // slice up the texture and pool it
                BufferedImage slice = bi.getSubimage(current, 0, Math.min(128, bi.getWidth() - current), bi.getHeight());
                sliced.add(new IndexedTexture.GeneratedTexture(symbol.current++, "lore_design", slice.getWidth(), slice, bi.getHeight()));
                // move our pointer ahead
                current += Math.min(128, bi.getWidth());
            }
            // offer up the texture we generated
            output.put(prefix + "_" + (i / 10), IndexedTexture.GeneratedCompoundTexture.build(sliced));
        }

        return output;
    }
}
