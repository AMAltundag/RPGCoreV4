package me.blutkrone.rpgcore.npc;

import me.blutkrone.external.juliarn.npc.NPC;
import me.blutkrone.external.juliarn.npc.NPCPool;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCHideEvent;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCInteractEvent;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCShowEvent;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hologram.impl.Hologram;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.npc.EditorNPC;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages miscellaneous entities, such as vendors or quest
 * giving NPC entities.
 *
 * @see NPCPool https://github.com/juliarn/NPC-Lib/blob/development/LICENSE
 */
public class NPCManager implements Listener {

    private NPCPool pool;
    private EditorIndex<CoreNPC, EditorNPC> index;
    private Map<String, StoragePage> storage = new HashMap<>();

    // NPC mapped to its hologram entity
    private Map<NPC, Hologram> hologram = new HashMap<>();
    // npc mapped to its core template
    private Map<NPC, CoreNPC> design = new HashMap<>();
    // npc mapped to the acitve node identifier
    private Map<NPC, UUID> origin = new HashMap<>();

    public NPCManager() {
        // construct a pool to build our NPC entities with
        this.pool = NPCPool.builder(RPGCore.inst())
                .spawnDistance(60)
                .actionDistance(30)
                .tabListRemoveTicks(20)
                .build();
        // setup the index for the NPCs
        this.index = new EditorIndex<>("npc", EditorNPC.class, EditorNPC::new);
        // load respective storage pages
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("storage.yml"));
            config.forEachUnder("storages", (id, root) -> {
                this.storage.put(id, new StoragePage(id, root.getSection(id)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        // update NPC names per player once a second
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            for (NPC npc : pool.getNPCs()) {
                CoreNPC core_npc = design.get(npc);
                if (core_npc != null) {
                    Hologram hologram = this.hologram.get(npc);
                    if (hologram != null) {
                        for (Player player : npc.getSeeingPlayers()) {
                            hologram.name(player, core_npc.describe(player));
                        }
                    }
                }
            }
        }, 20, 20);

        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    /**
     * Retrieve the relevant storage.
     *
     * @param id what storage to retrieve
     * @return the storage we retrieved
     */
    public StoragePage getStorage(String id) {
        return this.storage.get(id);
    }

    /**
     * An index tracking NPC templates.
     *
     * @return NPC template index.
     */
    public EditorIndex<CoreNPC, EditorNPC> getIndex() {
        return index;
    }

    /**
     * Retrieve the design the NPC was created with.
     *
     * @param npc which NPC to check
     * @return the design that created the NPC
     */
    public CoreNPC getDesign(NPC npc) {
        return this.design.get(npc);
    }

    /**
     * The UUID of the active node instance which spawned the
     * NPC.
     *
     * @param npc which NPC to check
     * @return the origin of the NPC, may not exist.
     */
    public UUID getOrigin(NPC npc) {
        return this.origin.get(npc);
    }

    /**
     * Create an NPC at the given location.
     *
     * @param npc   the NPC we want to create
     * @param where where to create the NPC
     * @return the created NPC instance
     */
    public NPC create(String npc, Location where) {
        CoreNPC core_npc = getIndex().get(npc);
        NPC created = core_npc.create(this.pool, where);
        this.design.put(created, core_npc);
        return created;
    }

    /**
     * Create an NPC at the given location.
     *
     * @param npc    the NPC we want to create
     * @param where  where to create the NPC
     * @param origin node that created the NPC
     * @return the created NPC instance
     */
    public NPC create(String npc, Location where, NodeActive origin) {
        CoreNPC core_npc = getIndex().get(npc);
        NPC created = core_npc.create(this.pool, where);
        this.design.put(created, core_npc);
        this.origin.put(created, origin.getID());
        return created;
    }

    /**
     * Unregister the NPC instance.
     *
     * @param npc which NPC to unregister.
     */
    public void remove(NPC npc) {
        this.pool.removeNPC(npc.getEntityId());
        // drop the trackers for the NPC
        this.design.remove(npc);
        this.origin.remove(npc);
        Hologram remove = this.hologram.remove(npc);
        if (remove != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                remove.destroy(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void on(PlayerNPCShowEvent e) {
        Hologram hologram = this.hologram.computeIfAbsent(e.getNPC(), (k -> new Hologram()));
        // spawn the hologram
        hologram.spawn(e.getPlayer(), e.getNPC().getLocation());
        // construct an appropriate name
        CoreNPC core_npc = design.get(e.getNPC());
        hologram.name(e.getPlayer(), core_npc.describe(e.getPlayer()));
        // attach it to the NPC
        hologram.mount(e.getPlayer(), e.getNPC().getEntityId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void on(PlayerNPCHideEvent e) {
        Hologram hologram = this.hologram.computeIfAbsent(e.getNPC(), (k -> new Hologram()));
        // destruct hologram while out of range
        hologram.destroy(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void on(PlayerNPCInteractEvent e) {
        // this is handled by NodeManager to cover node removal
    }
}