package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.item.refinement.CoreRefinerRecipe;
import me.blutkrone.rpgcore.item.refinement.RefinementMenuDesign;
import me.blutkrone.rpgcore.npc.trait.impl.CoreRefinerTrait;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

public class RefinerMenu extends AbstractCoreMenu {

    private final CoreRefinerTrait trait;
    private RefinementMenuDesign design;
    private double speed;
    private List<CoreRefinerRecipe> recipes;
    private int timer;
    private int wanted_snapshot;
    private CoreRefinerRecipe recipe;

    private ItemStack invisible;

    public RefinerMenu(CoreRefinerTrait origin, RefinementMenuDesign design, List<CoreRefinerRecipe> recipes) {
        super(6);

        this.design = design;
        this.speed = origin.speed;
        this.recipes = recipes;
        this.trait = origin;

        this.invisible = language().getAsItem("invisible").build();
        this.timer = 0;
        this.wanted_snapshot = 0;
    }

    @Override
    public void rebuild() {
        // create basic menu layout
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_" + design.getMenu()), ChatColor.WHITE);

        if (recipe != null) {
            int total_frames = design.getAnimationSize() - 1;
            if (total_frames > 0) {
                // identify tick time snapshots
                int time_need = (int) (recipe.getDuration() / (1d + speed));
                // identify progress ratio
                double progress;
                if (timer > time_need) {
                    progress = 1d;
                } else {
                    progress = Math.max(0d, (0d + timer) / (0d + time_need));
                }
                // overlay with animation
                int frame_to_use = (int) (progress * total_frames);
                msb.shiftToExact(-208);
                msb.append(resourcepack().texture("menu_" + design.getMenu() + "_progress_" + frame_to_use), ChatColor.WHITE);
            }
        }

        // provide instructions on usage
        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_refinement"));
        instructions.apply(msb);

        // supply the title to the player
        getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        // this fixes some weird de-sync issues
        getMenu().stalled(() -> ((Player) event.getWhoClicked()).updateInventory());

        if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.RIGHT) {
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                if (design.getInputs().contains(event.getSlot())) {
                    // reset timer upon interaction
                    timer = 0;

                    ItemStack cursor = event.getCursor();
                    if (cursor == null || cursor.getType().isAir()) {
                        // always allow taking item out of the slot
                        event.setCancelled(false);
                    } else {
                        // input slots accept all items marked "REFINEMENT"
                        ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(cursor, ItemDataGeneric.class);
                        event.setCancelled(data == null || !data.getItem().getTags().contains("unrefined"));
                    }
                } else if (design.getOutputs().contains(event.getSlot())) {
                    if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
                        // only allow taking items out of these slots
                        ItemStack clicked = event.getCurrentItem();
                        ItemStack cursor = event.getCursor();
                        if (clicked == null || clicked.getType().isAir()) {
                            // do not pick up air
                            event.setCancelled(true);
                        } else if (cursor != null && !cursor.getType().isAir()) {
                            // do not drop items in
                            event.setCancelled(true);
                        } else {
                            // allow taking out items
                            event.setCancelled(false);
                        }
                    }
                } else {
                    // unknown slot, deny for safety
                    event.setCancelled(true);
                }
            } else {
                if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
                    // basic pick-up from lower inventory is allowed
                    event.setCancelled(false);
                } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                    // reset timer upon interaction
                    timer = 0;

                    // shift clicking is manually handled
                    event.setCancelled(true);

                    ItemStack clicked = event.getCurrentItem();
                    ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(clicked, ItemDataGeneric.class);
                    if (data != null && data.getItem().getTags().contains("unrefined")) {
                        if (clicked != null && !clicked.getType().isAir()) {
                            // shift-left is manually handled
                            event.setCancelled(true);
                            for (Integer slot : design.getInputs()) {
                                // we done if nothing is left
                                if (clicked.getAmount() <= 0) {
                                    break;
                                }

                                ItemStack previous = event.getView().getTopInventory().getItem(slot);
                                if (previous == null || previous.getType().isAir()) {
                                    // populate the slot itself
                                    event.getView().getTopInventory().setItem(slot, clicked);
                                    clicked.setAmount(0);
                                } else if (previous.getAmount() > 1 && previous.isSimilar(clicked)) {
                                    // attempt to absorb into existing stack
                                    int absorb = Math.min(clicked.getAmount(), previous.getMaxStackSize() - previous.getAmount());
                                    previous.setAmount(previous.getAmount() + absorb);
                                    clicked.setAmount(clicked.getAmount() - absorb);
                                }
                            }
                        }
                    }
                } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                    // do not allow shift-right
                    event.setCancelled(true);
                } else {
                    // cancelled for safety sake
                    event.setCancelled(true);
                }
            }
        } else {
            // only allow 3 basic clicks here
            event.setCancelled(true);
        }
    }

    @Override
    public void open(InventoryOpenEvent event) {
        deserialize();

        // placeholders to make shift-clicks look nicer
        Set<Integer> ignore = new HashSet<>();
        ignore.addAll(design.getInputs());
        ignore.addAll(design.getOutputs());
        for (int i = 0; i < 54; i++) {
            if (!ignore.contains(i)) {
                ItemStack previous = getMenu().getItemAt(i);
                if (previous == null || previous.getType().isAir()) {
                    getMenu().setItemAt(i, invisible);
                }
            }
        }
    }

    @Override
    public void close(InventoryCloseEvent event) {
        // save data from the refiner
        serialize();
    }

    @Override
    public void tick() {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        if (core_player == null) {
            return;
        }

        // actual crafting only happens once a second
        if (timer++ % 20 == 0) {
            // update the active recipe
            if (recipe == null) {
                recipe = getRecipe();
            }

            // one output slot must be empty
            int first_empty = -1;
            for (int slot : design.getOutputs()) {
                ItemStack item = getMenu().getItemAt(slot);
                if (item == null || item.getType().isAir()) {
                    first_empty = slot;
                    break;
                }
            }

            // process recipe if we got one
            if (recipe != null) {
                // play the progress effect
                recipe.playEffectWorking(getMenu().getViewer());
                // dump recipe if we cannot afford it
                List<ItemStack> materials = new ArrayList<>();
                for (int slot : design.getInputs()) {
                    ItemStack ingredient = getMenu().getItemAt(slot);
                    if (ingredient != null && !ingredient.getType().isAir()) {
                        materials.add(ingredient);
                    }
                }

                // check if we've archived timer condition
                int wanted_time = (int) (recipe.getDuration() / (1d + speed));
                if (timer >= wanted_time) {
                    if (recipe.craftAndConsume(1, materials) == 0) {
                        // cannot afford cost of recipe anymore, drop it
                        this.recipe = null;
                    } else {
                        // pick the item we want to acquire
                        CoreItem subject = recipe.getRandomOutput();
                        if (subject != null) {
                            ItemStack stack = subject.acquire(core_player, 0d);
                            if (subject.isUnstackable()) {
                                // dump on first empty slot
                                getMenu().setItemAt(first_empty, stack);
                            } else {
                                // merge into an existing slot
                                for (int slot : design.getOutputs()) {
                                    if (stack.getAmount() <= 0) {
                                        break;
                                    }

                                    ItemStack existing = getMenu().getItemAt(slot);
                                    if (existing != null && existing.isSimilar(stack)) {
                                        int absorb = Math.min(stack.getAmount(), stack.getMaxStackSize() - stack.getAmount());
                                        stack.setAmount(stack.getAmount() - absorb);
                                        existing.setAmount(existing.getAmount() + absorb);
                                    }
                                }
                                // pool on stack if we got left-overs
                                if (stack.getAmount() > 0) {
                                    getMenu().setItemAt(first_empty, stack);
                                }
                            }
                        }
                    }

                    // reset the timer
                    timer = 0;
                }

            }
        }

        getMenu().queryRebuild();
    }

    /*
     * Deserialize the items which belong to this refiner.
     *
     * @param core_player whose items are we deserializing
     * @param menu which menu to deserialize into
     */
    private void deserialize() {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

        // deserialize input items
        String inputs = core_player.getStoredItems().remove("refinement_input_" + trait.design + "_" + trait.inventory);
        List<ItemStack> input_items = new ArrayList<>();
        if (inputs != null) {
            try {
                input_items.addAll(Arrays.asList(BukkitSerialization.fromBase64(inputs)));
            } catch (IOException e1) {
                Bukkit.getLogger().severe("§cCorrupted B64 data: " + inputs + " could not be loaded, deleting!");
                e1.printStackTrace();
            }
        }

        // deserialize output items
        String outputs = core_player.getStoredItems().remove("refinement_output_" + trait.design + "_" + trait.inventory);
        List<ItemStack> output_items = new ArrayList<>();
        if (outputs != null) {
            try {
                output_items.addAll(Arrays.asList(BukkitSerialization.fromBase64(outputs)));
            } catch (IOException e1) {
                Bukkit.getLogger().severe("§cCorrupted B64 data: " + outputs + " could not be loaded, deleting!");
                e1.printStackTrace();
            }
        }

        // check if we got enough space to afk-generate
        Map<CoreItem, Integer> need_to_merge = new HashMap<>();
        if (output_items.size() < design.getOutputs().size()) {
            // identify how much time passed
            long timestamp_last_closed = core_player.getRefinementTimestamp().getOrDefault(trait.design + "_" + trait.inventory, 0L);
            int ticks_to_catch_up = (int) ((System.currentTimeMillis() - timestamp_last_closed) / 50);
            // provide with the AFK crafting results
            while (ticks_to_catch_up > 0 && !input_items.isEmpty()) {
                // delete materials if no amount left
                input_items.removeIf(item -> item.getType().isAir() || item.getAmount() <= 0);
                // check if we got any refine-able recipe
                CoreRefinerRecipe recipe = getRecipe(input_items);
                if (recipe == null) {
                    break;
                }
                // check how many copies we are able to refine
                int attempts = (int) (ticks_to_catch_up / (recipe.getDuration() / (1d + trait.speed)));
                if (attempts <= 0) {
                    break;
                }
                // query up a reward item and consume time
                int results = recipe.craftAndConsume(attempts, input_items);
                CoreItem output = recipe.getRandomOutput();
                if (output != null) {
                    need_to_merge.merge(output, results, (a, b) -> a + b);
                    // take off AFK time for next iteration
                    ticks_to_catch_up -= results * (recipe.getDuration() / (1d + trait.speed));
                }
            }
        }

        // merge the new items, excess is deleted.
        need_to_merge.forEach((item, amount) -> {
            if (item.isUnstackable()) {
                // if unstackable, just add a stack
                for (int i = 0; i < amount; i++) {
                    output_items.add(item.acquire(core_player, 0d));
                }
            } else {
                // if stacking, merge with another stack
                ItemStack stack = item.acquire(core_player, 0d);
                for (ItemStack other_item : output_items) {
                    if (other_item.isSimilar(stack)) {
                        int absorb = Math.min(amount, other_item.getMaxStackSize() - other_item.getAmount());
                        other_item.setAmount(other_item.getAmount() + absorb);
                        amount -= absorb;
                    }
                }
                // excess of items is provided as a new stack
                if (amount > 0) {
                    stack.setAmount(amount);
                    output_items.add(stack);
                }
            }
        });

        // populate menu with items, if we got any
        for (int i = 0; i < Math.min(input_items.size(), design.getInputs().size()); i++) {
            getMenu().setItemAt(design.getInputs().get(i), input_items.get(i));
        }
        for (int i = 0; i < Math.min(output_items.size(), design.getOutputs().size()); i++) {
            getMenu().setItemAt(design.getOutputs().get(i), output_items.get(i));
        }
    }

    /*
     * Serialize the items which belong to this refiner.
     *
     * @param core_player whose items are we serializing
     * @param menu which menu to serialize from
     */
    private void serialize() {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

        // serialize all the data from the handler
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot : design.getInputs()) {
            ItemStack item = getMenu().getItemAt(slot);
            if (item != null && !item.getType().isAir()) {
                inputs.add(item);
            }
        }
        List<ItemStack> outputs = new ArrayList<>();
        for (int slot : design.getOutputs()) {
            ItemStack item = getMenu().getItemAt(slot);
            if (item != null && !item.getType().isAir()) {
                outputs.add(item);
            }
        }
        // track on the player, along with a timestamp for a catch-up.
        core_player.getRefinementTimestamp().put(trait.design + "_" + trait.inventory, System.currentTimeMillis() - (timer * 50));
        core_player.getStoredItems().put("refinement_input_" + trait.design + "_" + trait.inventory, BukkitSerialization.toBase64(inputs.toArray(new ItemStack[0])));
        core_player.getStoredItems().put("refinement_output_" + trait.design + "_" + trait.inventory, BukkitSerialization.toBase64(outputs.toArray(new ItemStack[0])));
    }


    /*
     * Retrieve the highest priority recipe the player can create.
     *
     * @param materials What ingredients are being refined.
     * @return the recipe that is the result.
     */
    private CoreRefinerRecipe getRecipe(List<ItemStack> materials) {
        // check which recipe we archived
        CoreRefinerRecipe valuable = null;
        for (CoreRefinerRecipe recipe : recipes) {
            // skip non matching recipes
            if (!recipe.isMatched(materials)) {
                continue;
            }
            // retain most valuable recipe
            if (valuable == null || valuable.getPriority() < recipe.getPriority()) {
                valuable = recipe;
            }
        }

        // offer up the most valued craftable recipe.
        return valuable;
    }

    /*
     * Retrieve the highest priority recipe the player can create.
     *
     * @param core_player whose items are we refining
     * @param menu which menu are we refining from
     * @return
     */
    private CoreRefinerRecipe getRecipe() {
        // check if we got a refinement recipe
        List<ItemStack> materials = new ArrayList<>();
        for (int slot : design.getInputs()) {
            ItemStack item = getMenu().getItemAt(slot);
            materials.add(item == null ? new ItemStack(Material.AIR) : item);
        }

        // offer up the most valued craftable recipe.
        return getRecipe(materials);
    }
}
