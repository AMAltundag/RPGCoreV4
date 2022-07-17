package me.blutkrone.rpgcore.hud.editor;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.design.Design;
import me.blutkrone.rpgcore.hud.editor.design.DesignCategory;
import me.blutkrone.rpgcore.hud.editor.design.DesignElement;
import me.blutkrone.rpgcore.hud.editor.design.designs.DesignList;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Administrative menu to edit relevant components of the
 * core.
 */
public class EditorMenu {

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

    /**
     * Create and open an editor for the given index, the said
     * editor is allowed unrestricted access on the index.
     *
     * @param _player who will receive the editor
     * @param _index  the index we wish to edit.
     */
    public void edit(Player _player, EditorIndex _index) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        // establish data format of the editor
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, _player);
        menu.setData("focus", new FocusQueue());
        menu.setData("index", _index);

        // establish design specific format of editor
        Design _design = this.designs.computeIfAbsent(_index.getEditorClass(), Design::new);
        if (_design.getCategories().size() > 0) {
            menu.setData("category", _design.getCategories().get(0));
        }

        menu.setRebuilder((() -> {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
            // reset the itemization we have now
            menu.clearItems();
            // font builder to use for editor overview
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_editor_selection"), ChatColor.WHITE);
            // retrieve what we do index thorough
            FocusQueue focus = menu.getData("focus");
            EditorIndex index = menu.getData("index");
            // offer basic navigation elements
            menu.setItemAt(0, this.icon_open);
            menu.setItemAt(26, this.scroll_up);
            menu.setItemAt(53, this.scroll_down);
            menu.setItemAt(8, this.icon_back);


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
                            menu.setItemAt((i + 2) * 9, preview);
                            // slots 2-8 are invisible for the sake of text
                            for (int j = 1; j < 8; j++) {
                                ItemBuilder invisible = ItemBuilder.of(preview.clone());
                                invisible.inheritIcon(RPGCore.inst().getLanguageManager().getAsItem("invisible").build());
                                menu.setItemAt(((i + 2) * 9) + j, invisible.build());
                            }
                        }
                    }
                }
            } else {
                // build up further menu details
                FocusQueue.Focus focused = focus.header();
                if (focused instanceof FocusQueue.ListFocus) {
                    menu.setItemAt(0, this.icon_list_add);
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
                            menu.setItemAt((i + 2) * 9 + j, icon);
                        }
                        // offer a button to delete the element at the index
                        ItemStack icon_delete = this.icon_list_remove.clone();
                        IChestMenu.setBrand(icon_delete, RPGCore.inst(), "delete_index", String.valueOf(entry.getKey()));
                        menu.setItemAt((i + 2) * 9 + 7, icon_delete);
                    }
                    // offer instructions on the constraint of the list
                    IEditorConstraint constraint = ((FocusQueue.ListFocus) focused).getList().getConstraint();
                    InstructionBuilder instructions = new InstructionBuilder();
                    instructions.add(constraint.getInstruction());
                    instructions.apply(msb);
                    // present the instructions
                    menu.setTitle(msb.compile());
                } else if (focused instanceof FocusQueue.ScopedFocus) {
                    // identify which elements are viewable by the player
                    List<DesignElement> elements = new ArrayList<>(((FocusQueue.ScopedFocus) focused).category.getElements());
                    elements.removeIf(ele -> ele.isHidden(focused.bundle));
                    List<DesignElement> viewport = IChestMenu.getViewport(focus.getOffset(), 4, elements);
                    // write the relevant fields to be viewed
                    for (int i = 0; i < 4; i++) {
                        DesignElement viewport_element = viewport.get(i);
                        if (viewport_element != null) {
                            // text about the relevant category
                            String left = viewport_element.getDesignName();
                            String right = viewport_element.getDesignInfo(focused.bundle);
                            msb.shiftToExact(135 - Utility.measureWidthExact(right)).append(right, "editor_viewport_" + (i + 1));
                            msb.shiftToExact(27).append(left, "editor_viewport_" + (i + 1)); // todo trim left text to not overlap
                            // left-most slot will get the full itemized category
                            ItemStack preview = viewport_element.getDesignIcon(focused.bundle).clone();
                            IChestMenu.setBrand(preview, RPGCore.inst(), "select_element", viewport_element.getUUID().toString());
                            menu.setItemAt((i + 2) * 9, preview);
                            // slots 2-8 are invisible but retain their text
                            for (int j = 1; j < 8; j++) {
                                ItemBuilder invisible = ItemBuilder.of(preview.clone());
                                invisible.inheritIcon(RPGCore.inst().getLanguageManager().getAsItem("invisible").build());
                                menu.setItemAt(((i + 2) * 9) + j, invisible.build());
                            }
                        }
                    }
                } else {
                    // save/clone only with root elements
                    if (focused instanceof FocusQueue.RootFocus) {
                        menu.setItemAt(1, this.icon_save);
                        menu.setItemAt(2, this.icon_clone);
                    }

                    // from the bundle, narrow the scope to a category
                    Design design = designs.computeIfAbsent(focused.bundle.getClass(), Design::new);
                    // identify which categories are viewable by the player
                    List<DesignCategory> categories = new ArrayList<>(design.getCategories());
                    categories.removeIf(cat -> cat.isHidden(focused.bundle));
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
                            menu.setItemAt((i + 2) * 9, preview);
                            // slots 2-8 are invisible for the sake of text
                            for (int j = 1; j < 8; j++) {
                                ItemBuilder invisible = ItemBuilder.of(preview.clone());
                                invisible.inheritIcon(RPGCore.inst().getLanguageManager().getAsItem("invisible").build());
                                menu.setItemAt(((i + 2) * 9) + j, invisible.build());
                            }
                        }
                    }
                }

                InstructionBuilder instructions = new InstructionBuilder();
                instructions.add(focused.bundle.getInstruction());
                instructions.apply(msb);
            }

            // inform about the updated title
            menu.setTitle(msb.compile());
        }));
        menu.setClickHandler(event -> {
            // the event shouldn't ever be processed.
            event.setCancelled(true);
            if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.SHIFT_LEFT)
                return;
            // ignore the event if the selection is bad.
            if (event.getCurrentItem() == null) {
                return;
            }
            if (event.getCurrentItem().getType().isAir()) {
                return;
            }
            if (!event.getCurrentItem().hasItemMeta()) {
                return;
            }

            // retrieve what we do index thorough
            EditorIndex index = menu.getData("index");
            FocusQueue focus = menu.getData("focus");

            if (this.icon_list_add.isSimilar(event.getCurrentItem())) {
                // add element to list
                this.addNewElement(menu, ((FocusQueue.ListFocus) focus.header()));
            } else if (this.scroll_up.isSimilar(event.getCurrentItem())) {
                // update offset of viewport
                focus.setOffset(Math.max(focus.getOffset() - 1, 0));
                menu.rebuild();
            } else if (this.scroll_down.isSimilar(event.getCurrentItem())) {
                // update offset of viewport
                if (focus.header() == null) {
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
                    List<String> filtered_history = new ArrayList<>(core_player.getEditorHistory());
                    filtered_history.removeIf(history -> !index.has(history));
                    focus.setOffset(Math.min(focus.getOffset() + 1, filtered_history.size()));
                } else if (focus.header() instanceof FocusQueue.ScopedFocus) {
                    focus.setOffset(Math.min(focus.getOffset() + 1, ((FocusQueue.ScopedFocus) focus.header()).size()));
                } else if (focus.header() instanceof FocusQueue.ListFocus) {
                    focus.setOffset(Math.min(focus.getOffset() + 1, ((FocusQueue.ListFocus) focus.header()).size()));
                } else {
                    Design design = this.designs.computeIfAbsent(focus.header().bundle.getClass(), Design::new);
                    focus.setOffset(Math.min(focus.getOffset() + 1, design.getCategories().size()));
                }
                menu.rebuild();
            } else if (this.icon_open.isSimilar(event.getCurrentItem())) {
                // allow to select where we will edit
                this.createOrLoad(menu);
            } else if (this.icon_save.isSimilar(event.getCurrentItem())) {
                // create backup, save to disk
                this.saveToDisk(menu);
            } else if (this.icon_clone.isSimilar(event.getCurrentItem())) {
                // create copy of current and open that
                this.cloneCurrent(menu);
            } else if (this.icon_back.isSimilar(event.getCurrentItem())) {
                // return back to previous editing element
                if (focus.size() == 1) {
                    if (event.getClick() == ClickType.SHIFT_LEFT) {
                        this.backToPrevious(menu);
                    } else {
                        event.getWhoClicked().sendMessage("§cShift-Click to exit root element without saving changes!");
                    }
                } else {
                    this.backToPrevious(menu);
                }
            } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_category", "").isEmpty()) {
                // enter a category of the top-most current bundle
                String category = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_category", "");
                focusOnCategory(menu, category);
            } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_element", "").isEmpty()) {
                // enter an bundle of the top-most current bundle
                String element = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_element", "");
                focusOnElement(menu, element);
            } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_root", "").isEmpty()) {
                // pick from the history element
                String id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "select_root", "");
                IEditorRoot root = index.edit(id);
                menu.stalled(() -> {
                    focus.focus(id, root);
                    menu.rebuild();
                });
            } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "list_index", "").isEmpty()) {
                // edit the value at the given field
                int i = Integer.valueOf(IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "list_index", ""));
                editElementAt(menu, focus, i);
            } else if (!IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "delete_index", "").isEmpty()) {
                Bukkit.getLogger().severe("DROP ELEMENT!");
                // delete the value in the given position of the list
                int i = Integer.valueOf(IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "delete_index", ""));
                menu.stalled(() -> {
                    FocusQueue.ListFocus header = (FocusQueue.ListFocus) focus.header();
                    List<Object> container = header.getValues();
                    container.remove(i);
                    menu.rebuild();
                });
            }
        });
        menu.setCloseHandler(event -> {
            // do not close while we got anything in our header
            menu.stalled(() -> {
                Player viewer = menu.getViewer();
                if (viewer.getOpenInventory().getType() == InventoryType.CRAFTING) {
                    FocusQueue focus = menu.getData("focus");
                    if (focus.size() != 0) {
                        menu.open();
                        event.getPlayer().sendMessage("§cClose your current element before doing this!");
                    }
                }
            });
        });

        menu.open();
    }

    /*
     * Open a menu which can add an element, do note that this is intended for
     * a constraint addition - not to configure the value that is generated from
     * it.
     *
     * @param source
     * @param container
     */
    private void addNewElement(IChestMenu source, FocusQueue.ListFocus focus) {
        DesignList design_list = focus.getList();
        List<Object> container = focus.getValues();

        // do not add new elements if capped
        if (design_list.isSingleton() && container.size() != 0) {
            source.getViewer().sendMessage("§cList cannot have multiple elements!");
            return;
        }
        // if we are a mono type, no need to type inquire
        if (design_list.getConstraint().isMonoType()) {
            design_list.getConstraint().addElement(container, "mono_type");
            source.setData("offset", container.size());
            source.getViewer().sendMessage("§aA value was created and added to the list!");
            source.rebuild();
            return;
        }

        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        source.getViewer().closeInventory();

        // prepare an input for the constraint identifier
        ITextInput input = RPGCore.inst().getVolatileManager().createInput(source.getViewer());
        // empty item to enable writing into the editor
        input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name(" ").build());
        input.setUpdating((updated) -> {
            input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name("§f" + updated).build());
            MagicStringBuilder msb = new MagicStringBuilder();

            List<String> hints = new ArrayList<>();
            if (updated.isBlank()) {
                // failed since input value is too short
                msb.shiftToExact(-60).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                hints.addAll(design_list.getConstraint().getHint(""));
            } else if (design_list.getConstraint().isDefined(updated)) {
                // success since we are within constraint
                msb.shiftToExact(-60).append(rpm.texture("menu_input_fine"), ChatColor.WHITE);
            } else if (design_list.getConstraint().canExtend()) {
                // success since we are creating a new value
                msb.shiftToExact(-60).append(rpm.texture("menu_input_maybe"), ChatColor.WHITE);
                hints.addAll(design_list.getConstraint().getHint(updated));
            } else {
                msb.shiftToExact(-60).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                hints.addAll(design_list.getConstraint().getHint(updated));
            }

            // limit how many hints can be shown to size
            if (hints.size() > 4) {
                hints.subList(4, hints.size()).clear();
            }

            // present the hints generated to the user
            for (int i = 0; i < hints.size(); i++) {
                msb.shiftToExact(0).append(hints.get(i), "anvil_input_hint_" + (i + 1));
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add("§fList Selection");
            instructions.add("§fPick what type of value to add to the list");
            instructions.apply(msb);

            msb.shiftToExact(-45).append(design_list.getName(), "text_menu_title");
            input.setTitle(msb.compile());
        });
        input.setResponse((response) -> {
            // try adding the value to the list
            if (response.isBlank()) {
                // warn about input value being too short
                input.getViewer().sendMessage("§cInput too short, update discarded!");
            } else if (design_list.getConstraint().isDefined(response)) {
                // add a clean value to the container
                design_list.getConstraint().addElement(container, response);
                // scroll down so the added value is visible
                source.setData("offset", container.size());
                // inform about the successful operation
                input.getViewer().sendMessage("§aA value was added to the list!");
            } else if (design_list.getConstraint().canExtend()) {
                // extend constraint by the value
                design_list.getConstraint().extend(response);
                // add a clean value to the container
                design_list.getConstraint().addElement(container, response);
                // scroll down so the added value is visible
                source.setData("offset", container.size());
                // inform about the successful operation
                input.getViewer().sendMessage("§aA value was created and added to the list!");
            } else {
                // warn about not finding any value
                input.getViewer().sendMessage("§cInput value invalid, update discarded!");
            }
            // recover to the preceding page
            input.stalled(source::open);
        });
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-60).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
        msb.shiftToExact(-45).append(design_list.getName(), "text_menu_title");
        input.setTitle(msb.compile());
        input.open();
    }

    /*
     * Open the editor for a value on the list.
     *
     * @param source
     * @param container
     * @param index
     */
    private void editElementAt(IChestMenu source, FocusQueue focus, int index) {
        FocusQueue.ListFocus header = (FocusQueue.ListFocus) focus.header();
        List<Object> container = header.getValues();

        if (container.get(index) instanceof IEditorBundle) {
            // shift the focus to edit the selected bundle
            source.stalled(() -> {
                focus.focus((IEditorBundle) container.get(index));
                source.rebuild();
            });
        } else {
            // delegate the attempt to edit this value
            header.getList().getConstraint().editDelegate(source, container.get(index));
        }
    }

    /*
     * Deepen the focus on the given category.
     *
     * @param editor which menu to implement logic on
     * @param category which category to focus on
     */
    private void focusOnCategory(IChestMenu editor, String _uuid) {
        FocusQueue focus = editor.getData("focus");
        FocusQueue.Focus header = focus.header();
        UUID uuid = UUID.fromString(_uuid);

        // search for the category we picked
        Design design = designs.computeIfAbsent(header.bundle.getClass(), Design::new);
        DesignCategory match = null;
        for (DesignCategory category : design.getCategories()) {
            if (category.getUUID().equals(uuid)) {
                match = category;
            }
        }

        // lacking a match means the menu was setup weirdly
        if (match == null) {
            throw new IllegalArgumentException("Could not match with a category!");
        }

        // narrow the scope on this focus
        focus.scope(match);
        // re-populate based of the focus
        editor.rebuild();
    }

    /*
     * Open a sub-editor from the given element.
     *
     * @param editor which menu to implement logic on
     * @param element which element to focus on
     */
    private void focusOnElement(IChestMenu editor, String _uuid) {
        FocusQueue focus = editor.getData("focus");
        FocusQueue.ScopedFocus header = (FocusQueue.ScopedFocus) focus.header();
        UUID uuid = UUID.fromString(_uuid);

        // search for the category we picked
        DesignElement match = null;
        for (DesignElement element : header.category.getElements()) {
            if (element.getUUID().equals(uuid)) {
                match = element;
            }
        }

        // lacking a match means the menu was setup weirdly
        if (match == null) {
            throw new IllegalArgumentException("Could not match with a category!");
        }

        // present the editor for the given element
        DesignElement final_match = match;
        editor.stalled(() -> final_match.edit(header.bundle, editor.getViewer(), editor));
    }

    /*
     * Drop the focus by one level.
     *
     * @param editor which menu to implement logic on
     */
    private void backToPrevious(IChestMenu editor) {
        editor.stalled(() -> {
            FocusQueue focus = editor.getData("focus");
            // retreat by one layer
            focus.drop();
            // rebuild from the new layer
            editor.rebuild();
        });
    }

    /*
     * Edit an existing element, or create a new element.
     *
     * @param editor which menu to implement logic on
     */
    private void createOrLoad(IChestMenu editor) {
        editor.stalled(() -> {
            editor.getViewer().closeInventory();

            ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
            // create input string to demand the id
            ITextInput input = RPGCore.inst().getVolatileManager().createInput(editor.getViewer());
            input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name(" ").build());
            input.setUpdating((current) -> {
                EditorIndex index = editor.getData("index");

                input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name("§f" + current).build());

                MagicStringBuilder msb = new MagicStringBuilder();
                msb.shiftToExact(-60);

                if (current.isBlank()) {
                    msb.append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                } else {
                    // hint at having a valid input (red is bad)
                    List<String> hints = new ArrayList<>();
                    if (current.length() < 1) {
                        msb.append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                        hints.add("Input too short!");
                    } else if (index.has(current)) {
                        msb.append(rpm.texture("menu_input_fine"), ChatColor.WHITE);
                        hints.add("Confirm to edit!");
                    } else {
                        msb.append(rpm.texture("menu_input_maybe"), ChatColor.WHITE);
                        hints.addAll(index.getKeys());
                        hints.removeIf(hint -> !hint.startsWith(current));
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

                msb.shiftToExact(-45).append("Create or edit", "text_menu_title");

                InstructionBuilder instructions = new InstructionBuilder();
                instructions.add("§fCreate Or Edit");
                instructions.add("§fCreate or edit the element you selected.");
                instructions.add("§cConfirm by pressing ESC");
                instructions.apply(msb);

                input.setTitle(msb.compile());
            });
            input.setResponse(current -> {
                FocusQueue focus = editor.getData("focus");
                EditorIndex index = editor.getData("index");

                if (current.isBlank()) {
                    // non-valid input (empty)
                    input.stalled(editor::open);
                } else if (index.has(current)) {
                    // load an existing element
                    IEditorRoot element = index.edit(current);
                    focus.focus(current, element);
                    input.stalled(editor::open);
                    editor.getViewer().sendMessage("§cEditing existing element '" + current + "'!");

                    CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(input.getViewer());
                    player.getEditorHistory().remove(current);
                    player.getEditorHistory().add(current);
                } else {
                    // create a new element
                    focus.clear();
                    IEditorRoot element = (IEditorRoot) index.getEditorFactory().get();
                    element.setFile(FileUtil.file(index.getDirectory(), current + ".rpgcore"));
                    focus.focus(current, element);
                    input.stalled(editor::open);
                    editor.getViewer().sendMessage("§cCreated new element '" + current + "'!");

                    CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(input.getViewer());
                    player.getEditorHistory().remove(current);
                    player.getEditorHistory().add(current);
                }
            });
            // provide the basic title
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-60).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
            msb.shiftToExact(-45).append("Create or edit", "text_menu_title");
            input.setTitle(msb.compile());
            input.open();
        });
    }

    /*
     * Keep a back-up before saving to the disk and applying a
     * synchronization call.
     *
     * @param editor which menu to implement logic on
     */
    private void saveToDisk(IChestMenu editor) {
        EditorIndex index = editor.getData("index");
        FocusQueue focus = editor.getData("focus");

        FocusQueue.Focus header = focus.header();
        if (header instanceof FocusQueue.RootFocus) {
            String id = ((FocusQueue.RootFocus) header).getId();
            // allow in-memory version to be processed
            index.update(id, ((IEditorRoot) header.bundle).build(id));
            // save changes to disk
            try {
                // todo keep a version control of our changes
                Bukkit.getLogger().severe("not implemented (backup before save)");
                // apply the actual saving
                ((IEditorRoot) header.bundle).save();
            } catch (IOException e) {
                editor.getViewer().sendMessage("§cSomething went wrong while saving!");
                e.printStackTrace();
            }
        } else {
            editor.getViewer().sendMessage("§cThe header is not a root view!");
        }
    }

    /*
     * Create a clone of the current element and edit that.
     *
     * @param editor which menu to implement logic on
     */
    private void cloneCurrent(IChestMenu editor) {
        editor.stalled(() -> {
            editor.getViewer().closeInventory();

            ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
            // create input string to demand the id
            ITextInput input = RPGCore.inst().getVolatileManager().createInput(editor.getViewer());
            input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name(" ").build());
            input.setUpdating((current) -> {
                EditorIndex index = editor.getData("index");

                input.setItemAt(0, ItemBuilder.of(this.invisible.clone()).name("§f" + current).build());

                MagicStringBuilder msb = new MagicStringBuilder();
                msb.shiftToExact(-60);

                if (current.isBlank()) {
                    msb.append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                } else {
                    // hint at having a valid input (red is bad)
                    List<String> hints = new ArrayList<>();
                    if (current.length() < 1 || index.has(current)) {
                        msb.append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
                        hints.add("Cannot use this ID!");
                    } else {
                        msb.append(rpm.texture("menu_input_fine"), ChatColor.WHITE);
                        hints.add("Confirm to create!");
                    }

                    // present the hints to the writing user
                    for (int i = 0; i < hints.size(); i++) {
                        msb.shiftToExact(0).append(hints.get(i), "anvil_input_hint_" + (i + 1));
                    }
                }

                InstructionBuilder instructions = new InstructionBuilder();
                instructions.add("§fClone Element");
                instructions.add("§fCreate an exact copy of the element.");
                instructions.add("§cConfirm by pressing ESC");
                instructions.apply(msb);

                msb.shiftToExact(-45).append("Clone Element", "text_menu_title");
                input.setTitle(msb.compile());
            });
            input.setResponse(current -> {
                FocusQueue focus = editor.getData("focus");
                EditorIndex index = editor.getData("index");

                if (!current.isBlank() && !index.has(current)) {
                    // create a clone of what we got open
                    IEditorRoot header = (IEditorRoot) focus.header().bundle;
                    File before = header.getFile();
                    try {
                        header.setFile(FileUtil.file(index.getDirectory(), current + ".rpgcore"));
                        header.save();
                        focus.focus(current, header);
                        input.stalled(editor::open);
                        editor.getViewer().sendMessage("§cEditing existing element '" + current + "'!");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    header.setFile(before);
                }
            });
            input.open();
            // provide the basic title
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-60).append(rpm.texture("menu_input_bad"), ChatColor.WHITE);
            msb.shiftToExact(-45).append("New ID to utilize", "text_menu_title");
            // present the menu to the player
            input.setTitle(msb.compile());
            input.open();
        });
    }

}