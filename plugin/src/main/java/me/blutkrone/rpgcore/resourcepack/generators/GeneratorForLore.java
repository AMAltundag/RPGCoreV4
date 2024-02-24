package me.blutkrone.rpgcore.resourcepack.generators;

import me.blutkrone.rpgcore.resourcepack.generation.IGenerator;
import me.blutkrone.rpgcore.resourcepack.OngoingGeneration;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.Allocator;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.CombinedTexture;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.GeneratedTexture;
import me.blutkrone.rpgcore.util.io.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Custom styles for item lore.
 */
public class GeneratorForLore implements IGenerator {
    private static final File INPUT_LORE_STYLE = FileUtil.directory("resourcepack/input/lore/style");
    private static final File INPUT_LORE_ICON = FileUtil.directory("resourcepack/input/lore/icon");
    private static final File INPUT_LORE_JEWEL = FileUtil.directory("resourcepack/input/lore/jewel");
    private static final File INPUT_SKILLBAR = FileUtil.directory("resourcepack/input/skillbar");

    @Override
    public void generate(OngoingGeneration generation) throws Exception {
        Allocator allocator = generation.hud().allocator();

        for (File style_folder : INPUT_LORE_STYLE.listFiles()) {
            String id = style_folder.getName();

            // used to separate categories
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "body_separator.png")), "lore_body_separator_" + id);
            // bottom most piece with information
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "body_bottom.png")), "lore_body_bottom_" + id);
            // top-most piece with information
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "body_top.png")), "lore_body_top_" + id);
            // closes off the item lore
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "footer.png")), "lore_footer_" + id);
            // top-most piece, usually decorative, may hold item level
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "header.png")), "lore_header_" + id);
            // right below the header, usually has item name
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "title.png")), "lore_title_" + id);
            // highlights are for important information
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "highlight_top.png")), "lore_highlight_top_" + id);
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "highlight_separator.png")), "lore_highlight_separator_" + id);
            generateStylePiece(generation, allocator, ImageIO.read(new File(style_folder, "highlight_bottom.png")), "lore_highlight_bottom_" + id);
            // special offsets to cover hiding the original lore
            for (GeneratedTexture texture : generation.hud().fetch("lore_header_" + id + "_0")) {
                texture.offset += 20;
            }
            for (GeneratedTexture texture : generation.hud().fetch("lore_header_" + id + "_1")) {
                texture.offset += 10;
            }
            for (GeneratedTexture texture : generation.hud().fetch("lore_footer_" + id + "_1")) {
                texture.offset -= 10;
            }
        }

        // load up textures for icons shown in lore
        for (File file : FileUtil.buildAllFiles(INPUT_LORE_ICON)) {
            String name = file.getName().replace(".png", "");
            BufferedImage texture = ImageIO.read(file);
            generation.hud().register("lore_icon_" + name, CombinedTexture.combine(allocator, texture, texture.getHeight() - 1));
        }

        // load up textures for icons shown in lore
        for (File file : FileUtil.buildAllFiles(INPUT_LORE_JEWEL)) {
            String name = file.getName().replace(".png", "");
            BufferedImage texture = ImageIO.read(file);
            generation.hud().register("lore_jewel_" + name, CombinedTexture.combine(allocator, texture, texture.getHeight() - 1));
        }

        // load up textures for icons shown in lore
        for (File file : FileUtil.buildAllFiles(INPUT_SKILLBAR)) {
            String name = file.getName().replace(".png", "");
            BufferedImage texture = ImageIO.read(file);
            generation.hud().register("lore_skill_" + name, CombinedTexture.combine(allocator, texture, texture.getHeight() - 1));
        }
    }

    /*
     * Generate the style pieces in vertical slices that can be used as a
     * backdrop for your description.
     *
     * @param generation
     * @param allocator
     * @param raw
     * @param prefix
     */
    private static void generateStylePiece(OngoingGeneration generation, Allocator allocator, BufferedImage image, String prefix) {
        for (int i = 0; i < image.getHeight(); i += 10) {
            BufferedImage texture = image.getSubimage(0, i, image.getWidth(), 10);
            generation.hud().register(prefix + "_" + (i / 10), CombinedTexture.combine(allocator, texture, texture.getHeight() - 1));
        }
    }
}
