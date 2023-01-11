package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.job.CoreProfession;
import me.blutkrone.rpgcore.passive.CorePassiveTree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProfessionTreeMenu extends MultiTreeMenu {

    @Override
    public List<CorePassiveTree> getTrees(CorePlayer player) {
        // sort professions by highest levelled
        List<CoreProfession> professions = new ArrayList<>(RPGCore.inst().getJobManager().getIndexProfession().getAll());
        professions.sort(Comparator.comparingDouble(profession -> {
            int level = player.getProfessionLevel().getOrDefault(profession.getId(), 1);
            double have_exp = player.getProfessionExp().getOrDefault(profession.getId(), 0d);
            double want_exp = RPGCore.inst().getLevelManager().getExpToLevelUpProfession(level);
            return level + (have_exp / want_exp);
        }));
        // grab relevant passive trees
        List<CorePassiveTree> trees = new ArrayList<>();
        for (CoreProfession profession : professions) {
            trees.add(profession.getTree(player));
        }
        // offer them to the menu
        return trees;
    }
}
