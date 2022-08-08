package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.FocusQueue;
import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.design.Design;
import me.blutkrone.rpgcore.hud.editor.design.DesignCategory;
import me.blutkrone.rpgcore.hud.editor.design.DesignElement;
import me.blutkrone.rpgcore.hud.editor.design.designs.DesignList;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EditorMenu extends AbstractCoreMenu {

    // control elements we are to work with
    private ItemStack icon_open = ItemBuilder.of(Material.CRAFTING_TABLE)
            .name("§aOpen By ID")
            .appendLore("§fSearch or create element with an ID")
            .build();
    private ItemStack icon_save = ItemBuilder.of(Material.FURNACE)
            .name("§aSave Current")
            .appendLore("§fSave your changes and apply them")
            .build();
    private ItemStack icon_clone = ItemBuilder.of(Material.CAULDRON)
            .name("§aClone Current")
            .appendLore("§fCreate a copy with a different ID")
            .build();
    private ItemStack icon_back = ItemBuilder.of(Material.CAULDRON)
            .name("§aBack To Previous")
            .appendLore("§fReturn to previous element")
            .build();
    private ItemStack icon_list_add = ItemBuilder.of(Material.CRAFTING_TABLE)
            .name("§aAdd To List")
            .appendLore("§fAdd new element to list")
            .build();
    private ItemStack icon_list_remove = ItemBuilder.of(Material.BARRIER)
            .name("§aRemove From List")
            .appendLore("§fRemoves this element from the list")
            .build();
    private ItemStack invisible = RPGCore.inst().getLanguageManager()
            .getAsItem("invisible")
            .meta(meta -> ((Repairable) meta).setRepairCost(-1))
            .build();
    private ItemStack scroll_up = RPGCore.inst().getLanguageManager()
            .getAsItem("viewport_up")
            .build();
    private ItemStack scroll_down = RPGCore.inst().getLanguageManager()
            .getAsItem("viewport_down")
            .build();

    // cached designs of editable classes
    private Map<Class, Design> designs = new HashMap<>();
    private DesignCategory category = null;

    private EditorIndex index;
    private FocusQueue focus;

    public EditorMenu(EditorIndex<?, ?> index) {
        super(6);
        this.index = index;
        this.focus = new FocusQueue();

        // establish design specific format of editor
        Design design = this.designs.computeIfAbsent(index.getEditorClass(), Design::new);
        if (design.getCategories().size() > 0) {
            this.category = design.getCategories().get(0);
        }
    }

    @Override
    public void rebuild() {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        // reset the itemization we have now
        this.getMenu().clearItems();
        // font builder to use for editor overview
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_editor_selection"), ChatColor.WHITE);
        // offer basic navigation elements
        this.getMenu().setItemAt(0, this.icon_open);
        this.getMenu().setItemAt(26, this.scroll_up);
        this.getMenu().setItemAt(53, this.scroll_down);
        this.getMenu().setItemAt(8, this.icon_back);

        Bukkit.getLogger().severe("REBUILD LEN " + focus.size() + " FOCUS " + focus.header());

        if (focus.header() == null) {
            // since nothing is opened, offer from history.
            List<String> filtered_history = new ArrayList<>(core_player.getEditorHistory());
            filtered_history.removeIf(history -> !index.has(history));

            if (!filtered_history.isEmpty()) {
                List<String> viewport = IChestMenu.getViewport(focus.getOffset(), 4, filtered_history);
                for (int i = 0; i < 4; i++) {
                    // retrieve which element we are operating on
                    String id = viewport.get(i);
                    if (id == null) {
                        continue;
                    }

                    IEditorRoot root = index.edit(id);
                    if (root != null) {
                        // write the menu-specific string down
                        msb.shiftToExact(26).append(id, "editor_viewport_" + (i + 1));
                        // left-most slot will get the full itemized variant
                        ItemStack preview = root.getPreview();
                        IChestMenu.setBrand(preview, RPGCore.inst(), "select_root", id);
                        this.getMenu().setItemAt((i + 2) * 9, preview);
                        // slots 2-8 are invisible for the sake of text
                        for (int j = 1; j < 8; j++) {
                            ItemBuilder invisible = ItemBuilder.of(preview.clone());
                            invisible.inheritIcon(RPGCore.inst().getLanguageManager().getAsItem("invisible").build());
                            this.getMenu().setItemAt(((i + 2) * 9) + j, invisible.build());
                        }
                    }
                }
            }
        } else {
            // build up further menu details
            FocusQueue.Focus focused = focus.header();
            if (focused instanceof FocusQueue.ListFocus) {
                this.getMenu().setItemAt(0, this.icon_list_add);
                // fetch the viewport of visible entries
                Map<Integer, Object> viewport = ((FocusQueue.ListFocus) focused).getViewport(focus.getOffset(), 4);
                List<Map.Entry<Integer, Object>> entries = new ArrayList<>(viewport.entrySet());
                // render the viewport of our list
                for (int i = 0; i < entries.size(); i++) {
                    // make sure that the value actually exists
                    Map.Entry<Integer, Object> entry = entries.get(i);
                    // write a readable version of the value
                    String readable = ((FocusQueue.ListFocus) focused).getList().getConstraint().getPreview(entry.getValue());
                    msb.shiftToExact(26).append(readable, "editor_viewport_" + (i + 1));
                    // generate an itemization that can be interacted with
                    ItemStack icon = RPGCore.inst().getLanguageManager().getAsItem("invisible")
                            .name(readable).build();
                    // track the index of this element in the real list
                    IChestMenu.setBrand(icon, RPGCore.inst(), "list_index", String.valueOf(entry.getKey()));
                    // offer the clickable elements on the relevant row
                    for (int j = 0; j < 7; j++) {
                        this.getMenu().setItemAt((i + 2) * 9 + j, icon);
                    }
                    // offer a button to delete the element at the index
                    ItemStack icon_delete = this.icon_list_remove.clone();
                    IChestMenu.setBrand(icon_delete, RPGCore.inst(), "delete_index", String.valueOf(entry.getKey()));
                    this.getMenu().setItemAt((i + 2) * 9 + 7, icon_delete);
                }
                // offer instructions on the constraint of the list
                IEditorConstraint constraint = ((FocusQueue.ListFocus) focused).getList().getConstraint();
                InstructionBuilder instructions = new InstructionBuilder();
                instructions.add(constraint.getInstruction());
                instructions.apply(msb);
            } else if (focused instanceof FocusQueue.ScopedFocus) {
                // identify which elements are viewable by the player
                List<DesignElement> elements = new ArrayList<>(((FocusQueue.ScopedFocus) focused).getCategory().getElements());
                elements.removeIf(ele -> ele.isHidden(focused.getBundle()));
                List<DesignElement> viewport = IChestMenu.getViewport(focus.getOffset(), 4, elements);
                // write the relevant fields to be viewed
                for (int i = 0; i < 4; i++) {
                    DesignElement viewport_element = viewport.get(i);
                    if (viewport_element != null) {
                        // text about the relevant category
                        String left = viewport_element.getDesignName();
                        String right = viewport_element.getDesignInfo(focused.getBundle());
                        msb.shiftToExact(135 - Utility.measureWidthExact(right)).append(right, "editor_viewport_" + (i + 1));
                        msb.shiftToExact(27).append(left, "editor_viewport_" + (i + 1)); // todo trim left text to not overlap
                        // left-most slot will get the full itemized category
                        ItemStack preview = viewport_element.getDesignIcon(focused.getBundle()).clone();
                        IChestMenu.setBrand(preview, RPGCore.inst(), "select_element", viewport_element.getUUID().toString());
                        this.getMenu().setItemAt((i + 2) * 9, preview);
                        // slots 2-8 are invisible but retain their text
                        for (int j = 1; j < 8; j++) {
                            ItemBuilder invisible = ItemBuilder.of(preview.clone());
                            invisible.inheritIcon(RPGCore.inst().getLanguageManager().getAsItem("invisible").build());
                            this.getMenu().setItemAt(((i + 2) * 9) + j, invisible.build());
                        }
                    }
                }
            } else {
                // save/clone only with root elements
                if (focused instanceof FocusQueue.RootFocus) {
                    this.getMenu().setItemAt(1, this.icon_save);
                    this.getMenu().setItemAt(2, this.icon_clone);
                }

                // from the bundle, narrow the scope to a category
                Design design = designs.computeIfAbsent(focused.getBundle().getClass(), Design::new);
                // identify which categories are viewable by the player
                List<DesignCategory> categories = new ArrayList<>(design.getCategories());
                categories.removeIf(cat -> cat.isHidden(focused.getBundle()));
                List<DesignCategory> viewport = IChestMenu.getViewport(focus.getOffset(), 4, categories);
                // write the relevant fields to be viewed
                for (int i = 0; i < 4; i++) {
                    DesignCategory viewport_category = viewport.get(i);
                    if (viewport_category != null) {
                        // text about the relevant category
                        msb.shiftToExact(26).append(viewport_category.getName(), "editor_viewport_" + (i + 1));
                        // left-most slot will get the full itemized category
                        ItemStack preview = viewport_category.getIcon().clone();
                        IChestMenu.setBrand(preview, RPGCore.inst(), "select_category", viewport_category.getUUID().toString());
                        this.getMenu().setItemAt((i + 2) * 9, preview);
                        // slots 2-8 are invisible for the sake of text
                        for (int j = 1; j < 8; j++) {
                            ItemBuilder invisible = ItemBuilder.of(preview.clone());
                            invisible.inheritIcon(RPGCore.inst().getLanguageManager().getAsItem("invisible").build());
                            this.getMenu().setItemAt(((i + 2) * 9) + j, invisible.build());
                        }
                    }
                }
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(focused.getBundle().getInstruction());
            instructions.apply(msb);
        }

        // inform about the updated title
        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        // the event shouldn't ever be processed.
        event.setCancelled(true);
        // only left and shift-left clicks matter
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.SHIFT_LEFT)
            return;
        // ensure we got a relevant clicked item
        if (!isRelevant(event.getCurrentItem())) {
            return;
        }

        if (this.icon_list_add.isSimilar(event.getCurrentItem())) {
            // add element to list
            this.actionAddToList();
        } else if (this.scroll_up.isSimilar(event.getCurrentItem())) {
            // update offset of viewport
            focus.setOffset(Math.max(focus.getOffset() - 1, 0));
            this.getMenu().queryRebuild();
        } else if (this.scroll_down.isSimilar(event.getCurrentItem())) {
            // update offset of viewport
            if (focus.header() == null) {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(this.getMenu().getViewer());
                List<String> filtered_history = new ArrayList<>(core_player.getEditorHistory());
                filtered_history.removeIf(history -> !index.has(history));
                focus.setOffset(Math.min(focus.getOffset() + 1, filtered_history.size()));
            } else if (focus.header() instanceof FocusQueue.ScopedFocus) {
                focus.setOffset(Math.min(focus.getOffset() + 1, ((FocusQueue.ScopedFocus) focus.header()).size()));
            } else if (focus.header() instanceof FocusQueue.ListFocus) {
                focus.setOffset(Math.min(focus.getOffset() + 1, ((FocusQueue.ListFocus) focus.header()).size()));
            } else {
                Design design = this.designs.computeIfAbsent(focus.header().getBundle().getClass(), Design::new);
                focus.setOffset(Math.min(focus.getOffset() + 1, design.getCategories().size()));
            }
            this.getMenu().queryRebuild();
        } else if (this.icon_open.isSimilar(event.getCurrentItem())) {
            // allow to select where we will edit
            this.actionCreateOrLoad();
        } else if (this.icon_save.isSimilar(event.getCurrentItem())) {
            // create backup, save to disk
            this.actionSaveToDisk();
        } else if (this.icon_clone.isSimilar(event.getCurrentItem())) {
            // create copy of current and open that
            this.actionCreateClone();
        } else if (this.icon_back.isSimilar(event.getCurrentItem())) {
            // return back to previous editing element
            if (focus.size() == 1) {
                if (event.getClick() == ClickType.SHIFT_LEFT) {
                    this.actionBackToPrevious();
                } else {
                    event.getWhoClicked().sendMessage("§cShift-Click to exit root element without saving changes!");
                }
            } else {
                this.actionBackToPrevious();
            }
        } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_category", "").isEmpty()) {
            // enter a category of the top-most current bundle
            String category = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_category", "");
            setFocusToCategory(category);
        } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_element", "").isEmpty()) {
            // enter an bundle of the top-most current bundle
            String element = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_element", "");
            setFocusToElement(element);
        } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_root", "").isEmpty()) {
            // pick from the history element
            String id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_root", "");
            IEditorRoot root = index.edit(id);
            this.getMenu().stalled(() -> {
                focus.focus(id, root);
                this.getMenu().queryRebuild();
            });
        } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "list_index", "").isEmpty()) {
            // edit the value at the given field
            int i = Integer.valueOf(IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "list_index", ""));
            setFocusToList(i);
        } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "delete_index", "").isEmpty()) {
            Bukkit.getLogger().severe("DROP ELEMENT!");
            // delete the value in the given position of the list
            int i = Integer.valueOf(IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "delete_index", ""));
            this.getMenu().stalled(() -> {
                FocusQueue.ListFocus header = (FocusQueue.ListFocus) focus.header();
                List<Object> container = header.getValues();
                container.remove(i);
                this.getMenu().queryRebuild();
            });
        }
    }

    @Override
    public void close(InventoryCloseEvent event) {
        // do not close while we got anything in our header
        this.getMenu().stalled(() -> {
            Player viewer = this.getMenu().getViewer();
            if (viewer.getOpenInventory().getType() == InventoryType.CRAFTING) {
                if (focus.size() != 0) {
                    this.getMenu().open();
                    event.getPlayer().sendMessage("§cClose your current element before doing this!");
                    this.getMenu().stalled(() -> this.getMenu().queryRebuild());
                }
            }
        });
    }

    /*
     * Drop the focus by one level.
     *
     * @param editor which menu to implement logic on
     */
    private void actionBackToPrevious() {
        // retreat by one layer
        focus.drop();
        // rebuild from the new layer
        getMenu().queryRebuild();
    }

    /*
     * Keep a back-up before saving to the disk and applying a
     * synchronization call.
     *
     * @param editor which menu to implement logic on
     */
    private void actionSaveToDisk() {
        FocusQueue.Focus header = focus.header();
        if (header instanceof FocusQueue.RootFocus) {
            String id = ((FocusQueue.RootFocus) header).getId();
            // allow in-memory version to be processed
            index.update(id, ((IEditorRoot) header.getBundle()).build(id));
            // save changes to disk
            try {
                // todo keep a version control of our changes
                Bukkit.getLogger().severe("not implemented (backup before save)");
                // apply the actual saving
                ((IEditorRoot) header.getBundle()).save();
                // drop an element and attempt to save
                focus.drop();
                getMenu().queryRebuild();
                // inform about having saved
                getMenu().getViewer().sendMessage("§aSaved '" + id + "' to disk!");
            } catch (IOException e) {
                getMenu().getViewer().sendMessage("§cSomething went wrong while saving!");
                e.printStackTrace();
            }
        } else {
            getMenu().getViewer().sendMessage("§cThe focus is not a RootFocus!");
        }
    }

    /*
     * Open a menu which can add an element, do note that this is intended for
     * a constraint addition - not to configure the value that is generated from
     * it.
     *
     * @param source
     * @param container
     */
    private void actionAddToList() {
        FocusQueue.ListFocus header = (FocusQueue.ListFocus) focus.header();
        DesignList design_list = header.getList();
        List<Object> container = header.getValues();

        // do not add new elements if capped
        if (design_list.isSingleton() && container.size() != 0) {
            getMenu().getViewer().sendMessage("§cList cannot have multiple elements!");
            return;
        }
        // if we are a mono type, no need to type inquire
        if (design_list.getConstraint().isMonoType()) {
            design_list.getConstraint().addElement(container, "mono_type");
            focus.setOffset(container.size());
            getMenu().getViewer().sendMessage("§aA value was created and added to the list!");
            getMenu().queryRebuild();
            return;
        }

        getMenu().stalled(() -> {
            getMenu().getViewer().closeInventory();
            new Add(getMenu(), design_list, container)
                    .finish(getMenu().getViewer(), "");
        });
    }

    /*
     * Edit an existing element, or create a new element.
     *
     * @param editor which menu to implement logic on
     */
    private void actionCreateOrLoad() {
        new Load(getMenu()).finish(getMenu().getViewer(), "");
    }

    /*
     * Create a clone of the current element and edit that.
     *
     * @param editor which menu to implement logic on
     */
    private void actionCreateClone() {
        new Clone(getMenu()).finish(getMenu().getViewer(), "");
    }

    /*
     * Updates the focus of the editor on an element which is in
     * a list of editor bundles.
     *
     * @param index Which index was interacted with on the current list focus
     */
    private void setFocusToList(int index) {
        // ensure we've got a list-focus
        if (!(focus.header() instanceof FocusQueue.ListFocus)) {
            getMenu().getViewer().sendMessage("§cFocus is not a ListFocus!");
            return;
        }
        // ensure the element is something we can edit
        FocusQueue.ListFocus header = (FocusQueue.ListFocus) focus.header();
        List<Object> container = header.getValues();
        if (!(container.get(index) instanceof IEditorBundle)) {
            getMenu().getViewer().sendMessage("§cElement is not a IEditorBundle!");
            return;
        }
        // update the focus to the said element
        focus.focus((IEditorBundle) container.get(index));
        // rebuild the editor with the updated focus
        getMenu().queryRebuild();
    }

    /*
     * Focus on a category of the current element.
     *
     * @param category_uuid Unique identifier for a category
     */
    private void setFocusToCategory(String category_uuid) {
        FocusQueue.Focus header = focus.header();
        UUID uuid = UUID.fromString(category_uuid);

        // search for the category we picked
        Design design = designs.computeIfAbsent(header.getBundle().getClass(), Design::new);
        DesignCategory match = null;
        for (DesignCategory category : design.getCategories()) {
            if (category.getUUID().equals(uuid)) {
                match = category;
            }
        }

        // design could not match
        if (match == null) {
            getMenu().getViewer().sendMessage("§cCould not match with any Design!");
            return;
        }

        // narrow the scope on this focus
        focus.scope(match);
        // re-populate based of the focus
        getMenu().queryRebuild();
    }

    /*
     * Focus on an element of the current element.
     *
     * @param editor which menu to implement logic on
     * @param element which element to focus on
     */
    private void setFocusToElement(String _uuid) {
        FocusQueue.ScopedFocus header = (FocusQueue.ScopedFocus) focus.header();
        UUID uuid = UUID.fromString(_uuid);

        // search for the category we picked
        DesignElement match = null;
        for (DesignElement element : header.getCategory().getElements()) {
            if (element.getUUID().equals(uuid)) {
                match = element;
            }
        }

        // lacking a match means the menu was setup weirdly
        if (match == null) {
            getMenu().getViewer().sendMessage("§cCould not match with any DesignElement!");
            return;
        }

        // present the editor for the given element
        DesignElement final_match = match;
        getMenu().stalled(() -> {
            final_match.edit(header.getBundle(), getMenu().getViewer(), getMenu(), focus);
        });
    }

    /*
     * Input that will add an element to a list.
     */
    private class Add extends AbstractCoreInput {

        private IChestMenu parent;
        private DesignList design_list;
        private List<Object> container;

        public Add(IChestMenu parent, DesignList design_list, List<Object> container) {
            this.parent = parent;
            this.design_list = design_list;
            this.container = container;
        }

        @Override
        public void response(String text) {
            // try adding the value to the list
            if (text.isBlank()) {
                // warn about input value being too short
                getMenu().getViewer().sendMessage("§cInput too short, update discarded!");
            } else if (design_list.getConstraint().isDefined(text)) {
                // add a clean value to the container
                design_list.getConstraint().addElement(container, text);
                // scroll down so the added value is visible
                focus.setOffset(container.size());
                // inform about the successful operation
                getMenu().getViewer().sendMessage("§aA value was added to the list!");
            } else if (design_list.getConstraint().canExtend()) {
                // extend constraint by the value
                design_list.getConstraint().extend(text);
                // add a clean value to the container
                design_list.getConstraint().addElement(container, text);
                // scroll down so the added value is visible
                focus.setOffset(container.size());
                // inform about the successful operation
                getMenu().getViewer().sendMessage("§aA value was created and added to the list!");
            } else {
                // warn about not finding any value
                getMenu().getViewer().sendMessage("§cInput value invalid, update discarded!");
            }
            // recover to the preceding page
            EditorMenu.this.suggestOpen(this.parent);
        }

        @Override
        public void update(String text) {
            getMenu().setItemAt(0, RPGCore.inst().getLanguageManager().getAsItem("invisible").name("§f" + text).build());
            MagicStringBuilder msb = new MagicStringBuilder();

            List<String> hints = new ArrayList<>();
            if (text.isBlank()) {
                // failed since input value is too short
                msb.shiftToExact(-260).append(resourcepack().texture("menu_input_bad"), ChatColor.WHITE);
                hints.addAll(design_list.getConstraint().getHint(""));
            } else if (design_list.getConstraint().isDefined(text)) {
                // success since we are within constraint
                msb.shiftToExact(-260).append(resourcepack().texture("menu_input_fine"), ChatColor.WHITE);
            } else if (design_list.getConstraint().canExtend()) {
                // success since we are creating a new value
                msb.shiftToExact(-260).append(resourcepack().texture("menu_input_maybe"), ChatColor.WHITE);
                hints.addAll(design_list.getConstraint().getHint(text));
            } else {
                msb.shiftToExact(-260).append(resourcepack().texture("menu_input_bad"), ChatColor.WHITE);
                hints.addAll(design_list.getConstraint().getHint(text));
            }

            // limit how many hints can be shown to size
            if (hints.size() > 4) {
                hints.subList(4, hints.size()).clear();
            }

            // present the hints generated to the user
            for (int i = 0; i < hints.size(); i++) {
                msb.shiftToExact(0).append(hints.get(i), "anvil_input_hint_" + (i + 1));
            }

            msb.shiftToExact(-45).append(design_list.getName(), "text_menu_title");
            getMenu().setTitle(msb.compile());
        }
    }

    /*
     * Input that will load a new root element.
     */
    private class Load extends AbstractCoreInput {

        private IChestMenu parent;

        public Load(IChestMenu parent) {
            this.parent = parent;
        }

        @Override
        public void response(String text) {
            if (text.isBlank()) {
                // non-valid input (empty)
                suggestOpen(parent);
            } else if (index.has(text)) {
                // load an existing element
                IEditorRoot element = index.edit(text);
                focus.focus(text, element);
                suggestOpen(parent);
                getMenu().getViewer().sendMessage("§cEditing existing element '" + text + "'!");
                // update editor history
                CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
                player.getEditorHistory().remove(text);
                player.getEditorHistory().add(text);
            } else {
                // create a new element
                focus.clear();
                IEditorRoot element = (IEditorRoot) index.getEditorFactory().get();
                element.setFile(FileUtil.file(index.getDirectory(), text + ".rpgcore"));
                focus.focus(text, element);
                // inform and recover to previous menu
                suggestOpen(parent);
                getMenu().getViewer().sendMessage("§cCreated new element '" + text + "'!");
                // update editor history
                CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
                player.getEditorHistory().remove(text);
                player.getEditorHistory().add(text);
            }
        }

        @Override
        public void update(String text) {
            getMenu().setItemAt(0, language().getAsItem("invisible").name("§f" + text).build());

            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-260);

            if (text.isBlank()) {
                msb.append(resourcepack().texture("menu_input_bad"), ChatColor.WHITE);
            } else {
                // hint at having a valid input (red is bad)
                List<String> hints = new ArrayList<>();
                if (text.length() < 1) {
                    msb.append(resourcepack().texture("menu_input_bad"), ChatColor.WHITE);
                    hints.add("Input too short!");
                } else if (index.has(text)) {
                    msb.append(resourcepack().texture("menu_input_fine"), ChatColor.WHITE);
                    hints.add("Confirm to edit!");
                } else {
                    msb.append(resourcepack().texture("menu_input_maybe"), ChatColor.WHITE);
                    hints.addAll(index.getKeys());
                    hints.removeIf(hint -> !hint.startsWith(text));
                    hints.sort(Comparator.naturalOrder());
                }

                // trim maximum hints to a valid length
                if (hints.size() > 4) {
                    hints = hints.subList(0, 4);
                }
                // present the hints to the writing user
                for (int i = 0; i < hints.size(); i++) {
                    msb.shiftToExact(0).append(hints.get(i), "anvil_input_hint_" + (i + 1));
                }
            }

            getMenu().setTitle(msb.compile());
        }
    }

    /*
     * Input that will create a copy of the current element
     * and load it under the given ID.
     */
    private class Clone extends AbstractCoreInput {

        private IChestMenu parent;

        public Clone(IChestMenu parent) {
            this.parent = parent;
        }

        @Override
        public void response(String text) {
            if (!text.isBlank() && !index.has(text)) {
                // create a clone of what we got open
                IEditorRoot header = (IEditorRoot) focus.header().getBundle();
                File before = header.getFile();
                try {
                    header.setFile(FileUtil.file(index.getDirectory(), text + ".rpgcore"));
                    header.save();
                    focus.focus(text, header);
                    suggestOpen(parent);
                    getMenu().getViewer().sendMessage("§cEditing existing element '" + text + "'!");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                header.setFile(before);
            }
        }

        @Override
        public void update(String text) {
            getMenu().setItemAt(0, language().getAsItem("invisible").name("§f" + text).build());

            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-260);

            if (text.isBlank()) {
                msb.append(resourcepack().texture("menu_input_bad"), ChatColor.WHITE);
            } else {
                // hint at having a valid input (red is bad)
                List<String> hints = new ArrayList<>();
                if (text.length() < 1 || index.has(text)) {
                    msb.append(resourcepack().texture("menu_input_bad"), ChatColor.WHITE);
                    hints.add("Cannot use this ID!");
                } else {
                    msb.append(resourcepack().texture("menu_input_fine"), ChatColor.WHITE);
                    hints.add("Confirm to create!");
                }

                // present the hints to the writing user
                for (int i = 0; i < hints.size(); i++) {
                    msb.shiftToExact(0).append(hints.get(i), "anvil_input_hint_" + (i + 1));
                }
            }

            msb.shiftToExact(-45).append("Clone Element", "text_menu_title");
            getMenu().setTitle(msb.compile());
        }
    }

    private class ExitSave extends AbstractCoreMenu {

        public ExitSave(int size) {
            super(size);
        }

        @Override
        public void rebuild() {

        }

        @Override
        public void click(InventoryClickEvent event) {

        }
    }
}