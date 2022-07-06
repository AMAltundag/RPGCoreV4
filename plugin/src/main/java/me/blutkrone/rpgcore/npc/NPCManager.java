package me.blutkrone.rpgcore.npc;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.EditorNPC;
import org.bukkit.Location;

/**
 * Manages miscellaneous entities, such as vendors or quest
 * giving NPC entities.
 *
 * @see NPCPool https://github.com/juliarn/NPC-Lib/blob/development/LICENSE
 */
public class NPCManager {

    private NPCPool pool;
    private EditorIndex<CoreNPC, EditorNPC> index;

    public NPCManager() {
        // construct a pool to build our NPC entities with
        this.pool = NPCPool.builder(RPGCore.inst())
                .spawnDistance(60)
                .actionDistance(30)
                .tabListRemoveTicks(20)
                .build();
        // setup the index for the NPCs
        this.index = new EditorIndex<>("npc", EditorNPC.class, EditorNPC::new);
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
     * @param npc the NPC we want to create
     * @param where where to create the NPC
     * @return the created NPC instance
     */
    public NPC create(CoreNPC npc, Location where) {
        return npc.create(this.pool, where);
    }
}