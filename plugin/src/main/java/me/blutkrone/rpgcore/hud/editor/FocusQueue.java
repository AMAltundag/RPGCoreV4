package me.blutkrone.rpgcore.hud.editor;

import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.design.DesignCategory;
import me.blutkrone.rpgcore.hud.editor.design.designs.DesignList;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * A focus queue exists only for a singular element, under no
 * circumstances should the queue reference to a bundle which
 * is not a child of the initial element.
 */
public class FocusQueue {
    private List<Focus> queue = new ArrayList<>();
    private int scroll_offset;

    /**
     * Reset the queue and establish a new root element.
     *
     * @param root the element we are viewing.
     */
    public void focus(String id, IEditorRoot<?> root) {
        this.queue.clear();
        this.queue.add(new RootFocus(root, id));
        this.scroll_offset = 0;
    }

    /**
     * Shift focus on a bundle, this should give the user the option to
     * pick a category to narrow the selection into.
     *
     * @param bundle which bundle to put on focus.
     */
    public void focus(IEditorBundle bundle) {
        this.queue.add(new Focus(bundle));
        this.scroll_offset = 0;
    }

    /**
     * Shift the focus to a list,
     *
     * @param list
     */
    public void focus(DesignList list) {
        if (this.queue.isEmpty()) {
            return;
        }

        Focus peek = this.queue.get(this.queue.size() - 1);
        this.queue.add(new ListFocus(peek.bundle, list));
        this.scroll_offset = 0;
    }

    /**
     * Shift the focus on a category of the current bundle.
     *
     * @param category which category we are viewing.
     */
    public void scope(DesignCategory category) {
        if (this.queue.isEmpty()) {
            return;
        }

        Focus peek = this.queue.get(this.queue.size() - 1);
        if (peek instanceof ScopedFocus) {
            return;
        }

        this.queue.add(new ScopedFocus(peek.bundle, category));
        this.scroll_offset = 0;
    }

    /**
     * Fetch the highest level element we are focusing on, this may
     * be a scoped focus.
     *
     * @return the element we are focusing.
     */
    public Focus header() {
        return this.queue.isEmpty() ? null : this.queue.get(this.queue.size() - 1);
    }

    /**
     * Drop the focus by one level, we cannot drop the focus level
     * if we are already at the root element.
     */
    public void drop() {
        if (!this.queue.isEmpty()) {
            this.queue.remove(this.queue.size() - 1);
        }
    }

    /**
     * Clears any focus we have.
     */
    public void clear() {
        this.queue.clear();
    }

    /**
     * How many elements are in our focus queue.
     *
     * @return how many elements are in the focus queue.
     */
    public int size() {
        return this.queue.size();
    }

    /**
     * Fetch the offset on our current scope, this can be an
     * offset for categories of a bundle, or elements of the
     * category.
     *
     * @return current offset
     */
    public int getOffset() {
        return this.scroll_offset;
    }

    /**
     * Update the offset on our current scope.
     *
     * @param offset how far our offset is.
     */
    public void setOffset(int offset) {
        this.scroll_offset = offset;
    }

    /**
     * A focus on a bundle, this has to be narrowed to a scoped
     * focus before being edited in any form.
     */
    public static class Focus {
        final IEditorBundle bundle;

        Focus(IEditorBundle bundle) {
            this.bundle = bundle;
        }

        /**
         * Fetch which bundle we are focused on.
         *
         * @return the bundle we are operating on.
         */
        public IEditorBundle getBundle() {
            return bundle;
        }
    }

    /**
     * A focus on a root element.
     */
    public static class RootFocus extends Focus {
        final String id;

        RootFocus(IEditorRoot bundle, String id) {
            super(bundle);
            this.id = id;
        }

        /**
         * Which ID was the root element created with.
         *
         * @return the ID the root element was created with.
         */
        public String getId() {
            return id;
        }
    }

    /**
     * A focus on a list within an arbitrary bundle.
     */
    public static class ListFocus extends Focus {

        private final DesignList list;

        ListFocus(IEditorBundle bundle, DesignList list) {
            super(bundle);
            this.list = list;
        }

        /**
         * The list which is focused currently.
         *
         * @return the focused list.
         */
        public Map<Integer, Object> getViewport(int offset, int viewport) {
            return IChestMenu.getIndexedViewport(offset, viewport, this.getValues());
        }

        /**
         * The list connected to this focus.
         *
         * @return the connected list.
         */
        public DesignList getList() {
            return list;
        }

        /**
         * Fetch the list values associated with this queue.
         *
         * @return associated list values.
         */
        public List<Object> getValues() {
            return this.list.getList(this.getBundle());
        }

        /**
         * How many values are in the backing list.
         *
         * @return values in backing list.
         */
        public int size() {
            return getValues().size();
        }
    }

    /**
     * A view of a focused bundle, narrowed to a category.
     */
    public static class ScopedFocus extends Focus {

        final DesignCategory category;

        ScopedFocus(IEditorBundle bundle, DesignCategory category) {
            super(bundle);
            this.category = category;
        }

        /**
         * Fetch which category we narrowed our scope to.
         *
         * @return the category we are scoped to.
         */
        public DesignCategory getCategory() {
            return category;
        }

        /**
         * How many elements are in the category.
         *
         * @return elements in category
         */
        public int size() {
            return this.category.getElements().size();
        }
    }
}
