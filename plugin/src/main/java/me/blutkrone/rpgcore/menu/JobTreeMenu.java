package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.passive.CorePassiveTree;

import java.util.ArrayList;
import java.util.List;

public class JobTreeMenu extends MultiTreeMenu {

    @Override
    public List<CorePassiveTree> getTrees(CorePlayer player) {
        List<CorePassiveTree> trees = new ArrayList<>();
        for (String s : player.getJob().getPassiveTree()) {
            trees.add(RPGCore.inst().getPassiveManager().getTreeIndex().get(s));
        }
        return trees;
    }
}
