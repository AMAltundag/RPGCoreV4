package me.blutkrone.rpgcore.hud.editor;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.design.Design;
import me.blutkrone.rpgcore.hud.editor.design.DesignCategory;
import me.blutkrone.rpgcore.hud.editor.design.designs.DesignList;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.menu.EditorMenu;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A queue structure used by the editor, tracing the editing
 * of relevant elements.
 */
public class FocusQueue {

    private final EditorMenu menu;
    private List<AbstractFocus> queue = new ArrayList<>();

    /**
     * A queue structure used by the editor, tracing the editing
     * of relevant elements.
     *
     * @param menu the menu we are linked to
     * @param index the original index we are editing
     */
    public FocusQueue(EditorMenu menu, EditorIndex<?, ?> index) {
        this.menu = menu;
        this.queue.add(new NullFocus(index));
    }

    /**
     * Abandon the latest element on the queue, provided that it
     * isn't the null element. This also resets the offset.
     */
    public void drop() {
        if (this.queue.size() > 1) {
            this.queue.remove(this.queue.size()-1);
            getHeader().setScrollOffset(0);
        }
    }

    /**
     * The focus that has the highest position on our queue. This
     * is null if we haven't focused yet.
     *
     * @return current focus, or null.
     */
    public AbstractFocus getHeader() {
        if (this.queue.isEmpty()) {
            return null;
        } else {
            return this.queue.get(this.queue.size() - 1);
        }
    }

    /**
     * Reset the entire focus queue.
     */
    public void clearFocus() {
        this.queue.subList(1, this.queue.size()).clear();
    }

    /**
     * Put the focus on a bundle, do <b>not</b> use this to set the
     * focus for a root element.
     *
     * @param bundle the element we are focusing on.
     */
    public void setFocusToBundle(IEditorBundle bundle) {
        if (bundle instanceof IEditorRoot) {
            throw new IllegalArgumentException("Expected bundle, received root!");
        }

        this.queue.add(new ElementFocus(bundle));
    }

    /**
     * Set the focus on a list within the bundle.
     *
     * @param bundle the bundle that contains the list
     * @param list a design element that handles lists
     */
    public void setFocusToList(IEditorBundle bundle, DesignList list) {
        this.queue.add(new ListFocus(bundle, list));
    }

    /**
     * Set the focus on a root element, we may precede with other
     * root elements for cross reference related things.
     *
     * @param id the identifier of the root element.
     * @param root the element we are focusing on.
     * @param index the index we've pulled from.
     */
    public void setFocusToRoot(String id, IEditorRoot root, EditorIndex index) {
        this.queue.add(new ElementFocus(id, index, root));
    }

    /**
     * A common ancestor for everything which can be focused.
     */
    public abstract class AbstractFocus {
        int scroll_offset = 0;

        /**
         * The offset within the current focus, exact usage
         * differs based on implementation.
         *
         * @return offset within the focus.
         */
        public int getScrollOffset() {
            return scroll_offset;
        }

        /**
         * The offset within the current focus, exact usage
         * differs based on implementation.
         *
         * @param scroll_offset updated offset
         */
        public void setScrollOffset(int scroll_offset) {
            this.scroll_offset = scroll_offset;
        }

        /**
         * Size within which we can scroll, should this be -1 the focus
         * does does not know the size.
         *
         * @return size of the viewport
         */
        public abstract int getSize();

        /**
         * Grab the index from this focus, this may not exist.
         *
         * @return the index we've grabbed
         */
        public abstract EditorIndex getIndex();
    }

    /**
     * Original element on the focus queue, this cannot be
     * removed.
     */
    public class NullFocus extends AbstractFocus {

        private final EditorIndex<?, ?> index;

        public NullFocus(EditorIndex<?, ?> index) {
            this.index = index;
        }

        @Override
        public int getSize() {
            // size is the editing history of the user
            Player viewer = menu.getMenu().getViewer();
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(viewer);
            List<String> filtered_history = new ArrayList<>(core_player.getEditorHistory());
            filtered_history.removeIf(history -> !getIndex().has(history));
            return filtered_history.size();
        }

        @Override
        public EditorIndex getIndex() {
            return index;
        }
    }

    /**
     * A focus on an editor element, categorization can be used
     * to limit the scope we operate in.
     */
    public class ElementFocus extends AbstractFocus {
        String id;
        IEditorBundle bundle;
        DesignCategory category;

        Design design;

        EditorIndex index;

        /**
         * Used for bundle elements.
         *
         * @param bundle the bundle to focus
         */
        ElementFocus(IEditorBundle bundle) {
            this(null, null, bundle);
        }

        /**
         * Used for root elements.
         *
         * @param id root element identifier
         * @param bundle bundle or root element
         */
        ElementFocus(String id, EditorIndex index, IEditorBundle bundle) {
            this.id = id;
            this.bundle = bundle;
            this.design = menu.getDesigns().computeIfAbsent(bundle.getClass(), Design::new);
            this.index = index;
            if (design.getCategories().size() == 1) {
                this.category = this.design.getCategories().get(0);
            }
        }

        /**
         * Check if the bundle is a root element.
         *
         * @return true if bundle is a root element.
         */
        public boolean isRootElement() {
            return this.bundle instanceof IEditorRoot<?>;
        }

        /**
         * Check if the bundle is a root element where all elements
         * are visible to the user.
         *
         * @return true if all elements of a root bundle are visible.
         */
        public boolean isRootInFullView() {
            return this.bundle instanceof IEditorRoot<?>
                    && (this.category == null || this.getCategories().size() == 1);
        }

        /**
         * An ID is present, if we are a root element. Otherwise
         * this should offer an empty return value.
         *
         * @return ID for root elements.
         */
        public Optional<String> getId() {
            return Optional.ofNullable(this.id);
        }

        /**
         * A bundle or a root element.
         *
         * @return the element that is focused
         */
        public IEditorBundle getBundle() {
            return this.bundle;
        }

        /**
         * The current category, if this is null it means that the user
         * is present the category selection instead.
         *
         * @return the category we have.
         */
        public DesignCategory getCategory() {
            return this.category;
        }

        /**
         * Categproes allowed for this element.
         *
         * @return categories we have available.
         */
        public List<DesignCategory> getCategories() {
            return this.design.getCategories();
        }

        /**
         * Narrow the scope of the bundle.
         *
         * @param category updated category.
         */
        public void setCategory(DesignCategory category) {
            this.category = category;
            this.scroll_offset = 0;
        }

        /**
         * The design of the element we've focused.
         *
         * @return
         */
        public Design getDesign() {
            return design;
        }

        @Override
        public int getSize() {
            if (this.category == null) {
                return getCategories().size();
            } else {
                return this.category.getElements().size();
            }
        }

        @Override
        public EditorIndex getIndex() {
            return index;
        }
    }

    /**
     * A focus on a list within an element.
     */
    public class ListFocus extends AbstractFocus {
        IEditorBundle bundle;
        DesignList list;

        ListFocus(IEditorBundle bundle, DesignList list) {
            this.bundle = bundle;
            this.list = list;
            this.scroll_offset = 0;
        }

        /**
         * The bundle we are backed up by.
         *
         * @return backing bundle
         */
        public IEditorBundle getBundle() {
            return this.bundle;
        }

        /**
         * The design element of the list.
         *
         * @return backing design list.
         */
        public DesignList getDesignList() {
            return this.list;
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
        @Override
        public int getSize() {
            return getValues().size();
        }

        @Override
        public EditorIndex getIndex() {
            return null;
        }

        /**
         * The list which is focused currently.
         *
         * @return the focused list.
         */
        public Map<Integer, Object> getViewport(int offset, int viewport) {
            return IChestMenu.getIndexedViewport(offset, viewport, this.getValues());
        }
    }
}
