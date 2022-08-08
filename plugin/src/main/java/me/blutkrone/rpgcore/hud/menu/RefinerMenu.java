package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.refinement.CoreRefinerRecipe;
import me.blutkrone.rpgcore.item.refinement.RefinementMenuDesign;
import me.blutkrone.rpgcore.npc.trait.impl.CoreRefinerTrait;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefinerMenu {
    // designs used for a refinement menu
    private Map<String, RefinementMenuDesign> refinement_designs = new HashMap<>();

    public RefinerMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "refiner.yml"));
        config.forEachUnder("refiner", (path, root) -> {
            this.refinement_designs.put(path.toLowerCase(), new RefinementMenuDesign(path, root.getSection(path)));
        });
    }

    public void present(Player player, CoreRefinerTrait trait) {
        // extract basic parameters
        RefinementMenuDesign design = this.refinement_designs.get(trait.design);

        // ensure we got a core player to work with
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player.getUniqueId());
        if (core_player == null) {
            return;
        }
        // make sure we got a design we can process
        if (design == null) {
            player.sendMessage("§cBad config: The design '" + trait.design + "' does not exist!");
            return;
        }
        // make sure we got refinements registered
        List<CoreRefinerRecipe> recipes = trait.recipes.get();
        if (recipes.isEmpty()) {
            player.sendMessage("§cBad config, NPC trait has no recipes associated.");
            return;
        }

        new me.blutkrone.rpgcore.menu.RefinerMenu(trait, design, recipes).finish(player);
    }
}
