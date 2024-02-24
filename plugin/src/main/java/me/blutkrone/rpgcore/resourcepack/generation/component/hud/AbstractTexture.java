package me.blutkrone.rpgcore.resourcepack.generation.component.hud;

/**
 * The representation of a texture encoded into a font, this texture
 * object is intended to be identified by a key. The output given is
 * NOT expected to be stable post-generation.
 */
public abstract class AbstractTexture {

    // a numeral unique within the given table
    public String symbol;
    // which character table we are categorised under
    public String table;
    // width of this specific symbol
    public int width;
    // height of texture
    public int height;

    AbstractTexture(String symbol, String table, int width, int height) {
        this.symbol = symbol;
        this.table = table;
        this.width = width;
        this.height = height;
    }
}
