package me.blutkrone.rpgcore.hud.editor.index;

/**
 * An attachment which caches a value, until it the
 * linked index is updated. Do note that it is only
 * intended for processing <b>ONE!</b> index.
 *
 * @param <T> what index are we attached to
 * @param <Q> what value type do we cache to
 */
public abstract class IndexAttachment<T, Q> {

    // the index we are attached to
    private final EditorIndex<T, ?> index;
    // update snapshot from index
    private int last_update;
    // cached value from index
    private Q cached;

    /**
     * Create an attachment to an index which will
     * update when requested.
     *
     * @param index the index we're attaching to.
     */
    IndexAttachment(EditorIndex<T, ?> index) {
        this.index = index;
        this.last_update = -1;
        this.cached = null;
    }

    /**
     * Which index are we operating with.
     *
     * @return the index we are operating with.
     */
    public EditorIndex<T, ?> getIndex() {
        return index;
    }

    /**
     * Recompute the value in our cache, we call this method
     * when the index had any update.
     *
     * @return the new value to track.
     */
    protected abstract Q compute();

    /**
     * Retrieve the cached value, update if necessary.
     *
     * @return retrieve our indexed value.
     */
    public Q get() {
        if (this.cached == null || this.index.getVersion() != this.last_update) {
            this.cached = compute();
            this.last_update = this.index.getVersion();
        }
        return this.cached;
    }
}