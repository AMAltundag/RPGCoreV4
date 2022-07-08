package me.blutkrone.rpgcore.node.impl;

import com.github.juliarn.npc.NPC;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.root.EditorNodeSpawnerNPC;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.npc.CoreNPC;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class CoreNodeSpawnerNPC extends AbstractNode {

    private final String npc;

    public CoreNodeSpawnerNPC(String id, EditorNodeSpawnerNPC editor) {
        super(id, editor.permission, (int) editor.radius);
        this.npc = editor.npc;
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {
        Location where = new Location(world, active.getX(), active.getY(), active.getZ());

        // do not update if no players are nearby
        if (players.isEmpty()) {
            return;
        }

        // create data if missing
        NodeDataSpawnerNPC data = (NodeDataSpawnerNPC) active.getData();
        if (data == null) {
            // initial creation of the NPC
            data = new NodeDataSpawnerNPC();
            data.active = RPGCore.inst().getNPCManager().create(this.npc, where, active);
            active.setData(data);
        } else {
            // replace NPC if their template changed
            CoreNPC new_design = RPGCore.inst().getNPCManager().getIndex().get(this.npc);
            CoreNPC old_design = RPGCore.inst().getNPCManager().getDesign(data.active);

            if (old_design != new_design) {
                RPGCore.inst().getNPCManager().remove(data.active);
                data.active = RPGCore.inst().getNPCManager().create(this.npc, where, active);
            }
        }
    }

    @Override
    public void right(World world, NodeActive active, Player player) {

    }

    @Override
    public void left(World world, NodeActive active, Player player) {

    }

    /*
     * Data used to track a spawned NPC
     */
    private class NodeDataSpawnerNPC extends NodeData {

        // active instance of the npc
        private NPC active;

        NodeDataSpawnerNPC() {
        }

        @Override
        public void highlight(int time) {

        }

        @Override
        public void abandon() {
            RPGCore.inst().getNPCManager().remove(this.active);
        }
    }
}
