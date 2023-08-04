package me.blutkrone.rpgcore.nms.api.block;

import java.util.Arrays;

/**
 * A chunk outline is intended to trace out the shape of a chunk, the
 * shape being whether a block is occupied by a solid or not.
 */
public class ChunkOutline {
    private final boolean[][] shape;
    private byte flags;

    /**
     * Outline of a chunk.
     *
     * @param chunk_height Height of chunk.
     */
    public ChunkOutline(int chunk_height) {
        // 32 bytes can cover the entire array
        this.shape = new boolean[chunk_height][256];
    }

    /*
     * Outline of a chunk.
     * <br>
     * Used by copy method.
     *
     * @param shape
     * @param flags
     */
    ChunkOutline(boolean[][] shape, byte flags) {
        this.shape = shape;
        this.flags = flags;
    }

    /**
     * Create a copy of a chunk outline.
     *
     * @return Chunk outline.
     */
    public ChunkOutline copy() {
        boolean[][] shape = new boolean[this.shape.length][];
        for (int i = 0; i < this.shape.length; i++) {
            shape[i] = Arrays.copyOf(this.shape[i], this.shape[i].length);
        }

        return new ChunkOutline(shape, this.flags);
    }

    /**
     * A single byte meant to encode a state.
     *
     * @return Flag state we currently have
     */
    public byte getFlags() {
        return flags;
    }

    /**
     * A single byte meant to encode a state.
     *
     * @param flags Updated flag state
     */
    public void setFlags(byte flags) {
        this.flags = flags;
    }

    /**
     * Update the given position.
     *
     * @param x Local position in chunk.
     * @param y Local position in chunk.
     * @param z Local position in chunk.
     * @param flag Block is occupied.
     */
    public void set(int x, int y, int z, boolean flag) {
        this.shape[y][((x<<4)+z)] = flag;
    }

    /**
     * Check if a given block is occupied.
     *
     * @param x Local position in chunk.
     * @param y Local position in chunk.
     * @param z Local position in chunk.
     * @return Block is occupied.
     */
    public boolean get(int x, int y, int z) {
        return this.shape[y][((x<<4)+z)];
    }
}