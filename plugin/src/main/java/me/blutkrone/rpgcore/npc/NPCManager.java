package me.blutkrone.rpgcore.npc;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import me.blutkrone.external.juliarn.npc.NPCPool;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCInteractEvent;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.npc.EditorNPC;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.npc.trait.impl.CoreStorageTrait;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages miscellaneous entities, such as vendors or quest
 * giving NPC entities.
 *
 * @see NPCPool https://github.com/juliarn/NPC-Lib/blob/development/LICENSE
 */
public class NPCManager implements Listener {

    private NPCPool pool;
    private EditorIndex<CoreNPC, EditorNPC> index;
    private Map<String, CoreStorageTrait.StoragePage> storage = new HashMap<>();

    public NPCManager() {
        // construct a pool to build our NPC entities with
        this.pool = NPCPool.builder(RPGCore.inst()).spawnDistance(60).actionDistance(30).tabListRemoveTicks(20).build();
        // setup the index for the NPCs
        this.index = new EditorIndex<>("npc", EditorNPC.class, EditorNPC::new);
        // load respective storage pages
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("storage.yml"));
            config.forEachUnder("storages", (id, root) -> {
                this.storage.put(id, new CoreStorageTrait.StoragePage(id, root.getSection(id)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        // handle events related to NPCs
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    /**
     * Retrieve the relevant storage.
     *
     * @param id what storage to retrieve
     * @return the storage we retrieved
     */
    public CoreStorageTrait.StoragePage getStorage(String id) {
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
     * Create an NPC at the given location.
     *
     * @param npc   the NPC we want to create
     * @param where where to create the NPC
     * @return the created NPC instance
     */
    public ActiveCoreNPC create(String npc, Location where) {
        ActiveCoreNPC created = new ActiveCoreNPC(getIndex().get(npc), pool, where);
        this.pool.register(created);
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
    public ActiveCoreNPC create(String npc, Location where, NodeActive origin) {
        ActiveCoreNPC created = new ActiveCoreNPC(getIndex().get(npc), pool, where, origin.getID());
        this.pool.register(created);
        return created;
    }

    /**
     * Unregister the NPC instance.
     *
     * @param npc which NPC to unregister.
     */
    public void remove(AbstractPlayerNPC npc) {
        this.pool.remove(npc.id());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onInteractNodeNPC(PlayerNPCInteractEvent e) {
        // ensure we are not using world tool
        if (RPGCore.inst().getWorldIntegrationManager().isUsingTool(e.getPlayer())) {
            return;
        }
        // only one click should be detected
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        // delegate the interaction of the node if we got one
        e.getNPC().interact(e.getPlayer());
    }
}