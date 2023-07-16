package me.blutkrone.rpgcore.dungeon;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.dungeon.structure.TreasureStructure;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.editor.root.dungeon.EditorDungeon;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.metadata.MetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A manager for all dungeon specific logic.
 * <p>
 * todo: proper dungeon entrance, testing
 */
public class DungeonManager implements Listener {

    // index that tracks dungeon templates
    private EditorIndex<CoreDungeon, EditorDungeon> dungeon_index;
    // currently active dungeon instances
    private Map<String, IDungeonInstance> active = new HashMap<>();

    public DungeonManager() {
        this.dungeon_index = new EditorIndex<>("dungeon", EditorDungeon.class, EditorDungeon::new);

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            this.active.entrySet().removeIf(entry -> {
                IDungeonInstance instance = entry.getValue();
                // world was unloaded
                return instance.getWorld() == null || instance.update();
            });
        }, 1, 1);

        // every 10 minutes wipe excess worlds
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            File[] files = Bukkit.getWorldContainer().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && file.getName().startsWith("rpg_instance_")) {
                        try {
                            if (Bukkit.getWorld(file.getName()) == null) {
                                FileUtils.deleteDirectory(file);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 1, 12000);

        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    /**
     * Retrieves all instances currently active.
     *
     * @return Active instances.
     */
    public Map<String, IDungeonInstance> getInstances() {
        return active;
    }

    /**
     * An index which contains the templates for dungeons.
     *
     * @return Dungeon index.
     */
    public EditorIndex<CoreDungeon, EditorDungeon> getDungeonIndex() {
        return this.dungeon_index;
    }

    /**
     * Retrieve the dungeon instance attached to a world.
     *
     * @param world Potential dungeon world
     * @return Dungeon instance
     */
    public IDungeonInstance getInstance(World world) {
        if (world == null) {
            return null;
        }

        return this.active.get(world.getName());
    }

    /**
     * Retrieve the dungeon instance attached to a world.
     *
     * @param location Potential location in a dungeon world
     * @return Dungeon instance
     */
    public IDungeonInstance getInstance(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        return getInstance(location.getWorld());
    }

    /**
     * Prepare a dungeon instance that can be played in.
     *
     * @param content What dungeon to prepare.
     * @return An instance for the given content.
     */
    public IDungeonInstance createInstance(String content) {
        String id = ("rpg_instance_" + content + "_" + UUID.randomUUID().toString().replace("-", "")).toLowerCase();
        CoreDungeon template = this.getDungeonIndex().get(content);
        ActiveDungeonInstance instance = new ActiveDungeonInstance(id, template);
        this.active.put(id, instance);
        return instance;
    }

    /**
     * Prepare a dungeon instance for structure/build editing.
     *
     * @param content What dungeon to prepare.
     * @return An instance to perform editing in.
     */
    public IDungeonInstance editInstance(String content) {
        String id = ("rpg_instance_" + content + "_" + UUID.randomUUID().toString().replace("-", "")).toLowerCase();
        IndexAttachment<?, CoreDungeon> template = this.getDungeonIndex().getSoft(content);
        EditorDungeonInstance instance = new EditorDungeonInstance(id, template);
        this.active.put(id, instance);
        return instance;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    void onOfferTreasure(PlayerInteractAtEntityEvent event) {
        // ensure we are in a dungeon
        IDungeonInstance instance = getInstance(event.getPlayer().getWorld());
        if (!(instance instanceof ActiveDungeonInstance)) {
            return;
        }
        // check if we interacted with a treasure entity
        List<MetadataValue> rpgcore_treasure = event.getRightClicked().getMetadata("rpgcore_loot");
        if (rpgcore_treasure.isEmpty()) {
            return;
        }
        // check if player has treasure available
        UUID uuid = UUID.fromString(rpgcore_treasure.get(0).asString());
        TreasureStructure.ActiveTreasure treasure = ((ActiveDungeonInstance) instance).getTreasures().get(uuid);
        if (treasure == null) {
            return;
        }
        // loot entities cannot be really interacted with
        event.setCancelled(true);
        // handle the treasure chest
        treasure.offerTo(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onHideHiddenBlocks(ChunkLoadEvent event) {
        // strip hidden blocks from loaded chunks
        IDungeonInstance instance = getInstance(event.getWorld());
        if (instance instanceof ActiveDungeonInstance && ((ActiveDungeonInstance) instance).canHideChunk(event.getChunk())) {
            long chunk = (((long) event.getChunk().getX()) << 32) | event.getChunk().getZ();
            for (ActiveDungeonInstance.StructureTracker structure : ((ActiveDungeonInstance) instance).getStructures()) {
                List<Location> hidden = structure.hidden.get(chunk);
                if (hidden != null) {
                    for (Location where : hidden) {
                        where.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }
}