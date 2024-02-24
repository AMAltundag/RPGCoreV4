package me.blutkrone.rpgcore.resourcepack.generation.component.hud;

public class Allocator {
    private final String real_font_id;
    private char wordspace;

    public Allocator(String font_id) {
        this.real_font_id = font_id;
        this.wordspace = 0xFF;
    }

    /**
     * The font which we have allocated.
     *
     * @return The font allocated.
     */
    public String getFont() {
        return real_font_id;
    }

    /**
     * Retrieve the next character we have available.
     * <br>
     * Do note that this can overflow, special care should be
     * taken when handling access.
     *
     * @return Next character available.
     */
    public char getNext() {
        return wordspace++;
    }
}
