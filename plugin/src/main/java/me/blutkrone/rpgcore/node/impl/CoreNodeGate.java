package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.dungeon.CoreDungeon;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.root.node.EditorNodeGate;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.List;

public class CoreNodeGate extends AbstractNode {

    private final List<String> content;

    public CoreNodeGate(String id, EditorNodeGate editor) {
        super(id, (int) editor.radius, editor.getPreview());
        this.content = new ArrayList<>(editor.content);
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {
        for (Player player : players) {
            // ensure registered within core
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
            if (core_player == null) {
                continue;
            }
            // ensure no menu is open
            if (player.getOpenInventory().getType() != InventoryType.CRAFTING) {
                continue;
            }
            // verify which content types are fine
            List<String> content = new ArrayList<>();
            for (String id : this.content) {
                CoreDungeon template = RPGCore.inst().getDungeonManager().getDungeonIndex().get(id);
                if (template.canAccess(core_player)) {
                    content.add(id);
                }
            }
            // queue up into dungeons that are accessible
            RPGCore.inst().getSocialManager().getGroupHandler().queueForContent(player, content.toArray(new String[0]));
        }
    }

    @Override
    public void right(World world, NodeActive active, Player player) {

    }

    @Override
    public void left(World world, NodeActive active, Player player) {

    }
}