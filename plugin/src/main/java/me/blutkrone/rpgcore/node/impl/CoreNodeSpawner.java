package me.blutkrone.rpgcore.node.impl;

import me.blutkrone.rpgcore.hud.editor.root.node.EditorNodeSpawner;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class CoreNodeSpawner extends AbstractNode {

    public CoreNodeSpawner(String id, EditorNodeSpawner editor) {
        super(id, (int) editor.radius);

        Bukkit.getLogger().severe("not implemented (spawner node)");
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {

    }

    @Override
    public void right(World world, NodeActive active, Player player) {

    }

    @Override
    public void left(World world, NodeActive active, Player player) {

    }
}
