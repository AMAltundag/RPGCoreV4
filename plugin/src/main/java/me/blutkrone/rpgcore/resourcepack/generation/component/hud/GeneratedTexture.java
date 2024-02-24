package me.blutkrone.rpgcore.resourcepack.generation.component.hud;

import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * A texture that was generated during the current generation, do note that
 * the BufferedImage contained by this texture does not exist yet.
 * <br>
 * This object will only exist during the ongoing generation process, it
 * will become a {@link ConfiguredTexture} once done.
 */
public class GeneratedTexture extends AbstractTexture {
    // which texture we are backing up
    public BufferedImage texture;
    // vertical offset of texture
    public int offset;
    // identifier for where to dump texture
    public UUID uuid;

    public GeneratedTexture(Allocator allocator, BufferedImage texture, int offset) {
        super(String.valueOf(allocator.getNext()), allocator.getFont(), texture.getWidth(), texture.getHeight());
        this.texture = texture;
        this.offset = offset;
        this.uuid = UUID.randomUUID();
    }
}
