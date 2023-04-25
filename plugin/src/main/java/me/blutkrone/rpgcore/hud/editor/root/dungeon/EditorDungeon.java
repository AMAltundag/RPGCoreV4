package me.blutkrone.rpgcore.hud.editor.root.dungeon;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.dungeon.CoreDungeon;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.FocusQueue;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.AttributeAndFactorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.DungeonStructureConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.ItemConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.menu.EditorMenu;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EditorDungeon implements IEditorRoot<CoreDungeon> {

    private static ItemStack DUNGEON_EDIT_WORLD = ItemBuilder.of(Material.GRASS_BLOCK)
            .name("§fEdit World")
            .appendLore("§fEdit the dungeon world")
            .appendLore("§f[Shift+RMB] to interact with structures")
            .appendLore("§fClick existing structure to delete it")
            .appendLore("")
            .appendLore("§cOpen and click again to save and exit!")
            .appendLore("§cLogout or disconnect to save and exit!")
            .build();
    private static ItemStack DUNGEON_TEST_RUN = ItemBuilder.of(Material.DIAMOND_BOOTS)
            .name("§fTest Run")
            .appendLore("§fStart a test-run of this dungeon")
            .appendLore("")
            .appendLore("§cDo NOT do this with unsaved dungeons!")
            .appendLore("§cThis is a direct join, without matching!")
            .appendLore("§cCreate a gate node for a proper entrance!")
            .appendLore("§cWill start after exiting the editor!")
            .build();

    @EditorList(name = "Structure", constraint = DungeonStructureConstraint.class)
    @EditorTooltip(tooltip = {"RPGCore structures in the dungeon", "Can position while in edit mode"})
    public List<IEditorBundle> structures = new ArrayList<>();
    @EditorList(name = "Player Attribute", constraint = AttributeAndFactorConstraint.class)
    @EditorTooltip(tooltip = {"Attributes granted to players."})
    public List<IEditorBundle> player_attributes = new ArrayList<>();
    @EditorList(name = "Spawns Attribute", constraint = AttributeAndFactorConstraint.class)
    @EditorTooltip(tooltip = {"Attributes granted to spawned creatures."})
    public List<IEditorBundle> spawns_attributes = new ArrayList<>();
    @EditorList(name = "Keys", constraint = ItemConstraint.class)
    @EditorTooltip(tooltip = "All key items to open dungeon")
    public List<String> key_items = new ArrayList<>();
    @EditorWrite(name = "Icon", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Description item for dungeon", "§cThis is a language code, NOT plaintext."})
    public String lc_icon = "NOTHINGNESS";
    @EditorNumber(name = "Players", minimum = 1)
    @EditorTooltip(tooltip = {"How many players can enter dungeon"})
    public double player_limit = 6;
    @EditorNumber(name = "Min Lv", minimum = 1)
    @EditorTooltip(tooltip = {"Lowest level allowed"})
    public double minimum_level = 1;
    @EditorNumber(name = "Max Lv", minimum = 1)
    @EditorTooltip(tooltip = {"Highest level allowed"})
    public double maximum_level = 99;
    @EditorList(name = "Blacklist", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Condition cannot be fulfilled to enter.")
    public List<IEditorBundle> blacklist = new ArrayList<>();
    @EditorList(name = "Whitelist", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Condition must be fulfilled to enter.")
    public List<IEditorBundle> whitelist = new ArrayList<>();

    public transient File file;

    public EditorDungeon() {
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGsonPretty().toJson(this, fw);
        }
    }

    @Override
    public CoreDungeon build(String id) {
        return new CoreDungeon(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.SPAWNER)
                .name("§aDungeon")
                .build();
    }

    @Override
    public String getName() {
        return "Dungeon";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bDungeon");
        instruction.add("Instanced area accessible to players, a world with the");
        instruction.add("Same ID will be copied or created.");
        instruction.add("");
        instruction.add("Edit the dungeon world with '/rpg editdungeon <id>'");
        instruction.add("Save the dungeon world with '/rpg savedungeon'");
        instruction.add("Save also happens if you disconnect");
        return instruction;
    }

    @Override
    public List<ItemStack> getCustomControls() {
        return Arrays.asList(DUNGEON_EDIT_WORLD, DUNGEON_TEST_RUN);
    }

    @Override
    public boolean onCustomControl(EditorMenu menu, ItemStack item, ClickType click) {
        if (DUNGEON_TEST_RUN.isSimilar(item)) {
            if (click == ClickType.SHIFT_LEFT) {
                String editing_id = ((FocusQueue.ElementFocus) menu.getFocus().getHeader()).getId().get();
                IDungeonInstance instance = RPGCore.inst().getDungeonManager().createInstance(editing_id);
                instance.invite(Collections.singletonList(menu.getMenu().getViewer().getName()));
            } else {
                menu.getMenu().getViewer().sendMessage("§cUse [Shift+LMB], read warnings!");
            }
        } else if (DUNGEON_EDIT_WORLD.isSimilar(item)) {
            String editing_id = ((FocusQueue.ElementFocus) menu.getFocus().getHeader()).getId().get();

            IDungeonInstance current_instance = RPGCore.inst().getDungeonManager().getInstance(menu.getMenu().getViewer().getWorld());
            if (current_instance != null && current_instance.getTemplate().getId().equalsIgnoreCase(editing_id)) {
                // instance will save and sync when emptied out
                for (Player player : current_instance.getWorld().getPlayers()) {
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                    if (core_player != null && core_player.isInitiated()) {
                        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                            Location spawnpoint = core_player.getRespawnPosition();
                            player.teleport(spawnpoint);
                        });
                    }
                }
            } else {
                // prevent editing same world twice over
                for (IDungeonInstance instance : RPGCore.inst().getDungeonManager().getInstances().values()) {
                    String instance_id = instance.getTemplate().getId();
                    if (instance_id.equalsIgnoreCase(editing_id)) {
                        String players = instance.getWorld().getPlayers().stream()
                                .map(HumanEntity::getName)
                                .collect(Collectors.joining(","));
                        menu.getMenu().getViewer().sendMessage(String.format("§cCannot edit '%s', in use by: %s", instance_id, players));
                        return true;
                    }
                }

                // save to disk
                menu.actionSaveToDisk(true);
                // off-tick as to not de-sync
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    Player viewer = menu.getMenu().getViewer();
                    // close inventory
                    viewer.closeInventory();
                    // open in editing mode
                    IDungeonInstance instance = RPGCore.inst().getDungeonManager().editInstance(editing_id);
                    instance.invite(Collections.singletonList(viewer.getName()));
                });
            }


            return true;
        }

        return false;
    }
}
