package me.blutkrone.rpgcore.resourcepack.generation.component.hud;

import me.blutkrone.rpgcore.util.fontmagic.FontMagicConstant;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A combination of multiple generated textures, as to accommodate
 * the maximum width of 128 pixels per character.
 * <br>
 * This object will only exist during the ongoing generation process, it
 * will become a {@link ConfiguredTexture} once done.
 */
public class CombinedTexture extends AbstractTexture {

    private List<GeneratedTexture> textures;

    public CombinedTexture(String symbol, String table, int width, int height, List<GeneratedTexture> textures) {
        super(symbol, table, width, height);
        this.textures = textures;
    }

    /**
     * Create a texture object that holds the entire image, should we be wider
     * than 128 pixels this will be a combined texture otherwise it will just
     * be a {@link GeneratedTexture}
     * <br>
     *
     * @param allocator Allocator to reserve font space
     * @param image Texture that we want to encode
     * @param offset Vertical offset
     * @return A texture containing the image.
     */
    public static AbstractTexture combine(Allocator allocator, BufferedImage image, int offset) {
        if (image.getWidth() <= 128) {
            return new GeneratedTexture(allocator, image, offset);
        }

        // collect slices of 128 pixels
        List<GeneratedTexture> sliced = new ArrayList<>();
        int total_width = 0;
        while (total_width < image.getWidth()) {
            BufferedImage slice = image.getSubimage(total_width, 0, Math.min(128, image.getWidth() - total_width), image.getHeight());
            sliced.add(new GeneratedTexture(allocator, slice, offset));
            total_width += Math.min(128, image.getWidth());
        }

        // offer up the combined texture
        return combine(sliced);
    }

    /**
     * Combine the given textures into one object, should we only have
     * one texture we will return that one otherwise we will return an
     * combined instance representing that texture.
     *
     * @param textures
     * @return
     */
    public static AbstractTexture combine(List<GeneratedTexture> textures) {
        // having just one texture offers it up
        if (textures.size() == 1) {
            return textures.get(0);
        }

        // ensure we do use a single input
        String table = null;
        int width = 0;
        int height = 0;
        StringBuilder concat = new StringBuilder();

        Iterator<GeneratedTexture> iterator = textures.iterator();
        while (iterator.hasNext()) {
            AbstractTexture texture = iterator.next();
            // initialize the font we are using
            if (table == null) {
                table = texture.table;
            }
            // ensure font is not mismatching
            if (!table.equals(texture.table)) {
                throw new IllegalArgumentException(String.format("Mismatching tables '%s' and '%s'", table, texture.table));
            }
            // join the relevant parts
            width += texture.width;
            height = texture.height;
            concat.append(texture.symbol);
            // retreat by 1 pixel to fix auto offset
            if (iterator.hasNext()) {
                concat.append(FontMagicConstant.retreat(1));
            }
        }

        // concat into a single texture
        return new CombinedTexture(concat.toString(), table, width, height, textures);
    }

    /**
     * The textures which (directly) created this texture.
     *
     * @return the individual components of this texture,
     */
    public List<GeneratedTexture> getTextures() {
        return textures;
    }
}
