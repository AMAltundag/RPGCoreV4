package me.blutkrone.rpgcore.resourcepack.utils;

import me.blutkrone.rpgcore.util.fontmagic.FontMagicConstant;
import org.apache.commons.lang.StringEscapeUtils;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class IndexedTexture {
    // a numeral unique within the given table
    public String symbol;
    // which character table we are categorised under
    public String table;
    // width of this specific symbol
    public int width;

    private IndexedTexture(String symbol, String table, int width) {
        this.symbol = symbol;
        this.table = table;
        this.width = width;
    }

    IndexedTexture() {
    }

    @Override
    public String toString() {
        return String.format("IndexedTexture{symbol=%s;table=%s;width=%s}", StringEscapeUtils.escapeJava(symbol), table, width);
    }

    /**
     * A texture which was completely pre-generated and
     * only needs to be indexed.
     */
    public static class StaticTexture extends IndexedTexture {
        public StaticTexture(char symbol, String table, int width) {
            super(String.valueOf(symbol), table, width);
        }
    }

    /**
     * A texture that was generated previously
     */
    public static class ConfigTexture extends IndexedTexture {

        public ConfigTexture(String symbol, String table, int width) {
            super(symbol, table, width);
        }

        public ConfigTexture(IndexedTexture texture) {
            super(texture.symbol, texture.table, texture.width);
        }
    }

    /**
     * A texture that was freshly generated.
     */
    public static class GeneratedTexture extends IndexedTexture {
        // which texture we are backing up
        public BufferedImage texture;
        // vertical offset of texture
        public int offset;
        // width of the given symbol
        public int width;
        // an internal ID to prevent dupes
        public UUID uuid;

        public GeneratedTexture(char symbol, String table, int width, BufferedImage texture, int offset) {
            super(String.valueOf(symbol), table, width);
            this.texture = texture;
            this.offset = offset;
            this.width = width;
            this.uuid = UUID.randomUUID();
        }
    }

    /**
     * A container for multiple textures
     */
    public static class GeneratedCompoundTexture extends IndexedTexture {

        private final List<GeneratedTexture> textures;

        GeneratedCompoundTexture(String symbol, String table, int width, List<GeneratedTexture> textures) {
            super(symbol, table, width);
            this.textures = textures;
        }

        public static IndexedTexture build(List<GeneratedTexture> textures) {
            // having just one texture offers it up
            if (textures.size() == 1)
                return textures.get(0);
            // ensure we do use a single input
            String table = null;
            int width = 0;
            StringBuilder concat = new StringBuilder();

            Iterator<GeneratedTexture> iterator = textures.iterator();
            while (iterator.hasNext()) {
                IndexedTexture texture = iterator.next();
                if (table == null)
                    table = texture.table;
                if (!table.equals(texture.table))
                    throw new IllegalArgumentException(String.format("Mismatching tables '%s' and '%s'", table, texture.table));
                width += texture.width;
                concat.append(texture.symbol);

                if (iterator.hasNext())
                    concat.append(FontMagicConstant.retreat(1));
            }
            // concat into a single texture
            return new GeneratedCompoundTexture(concat.toString(), table, width, textures);
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
}
