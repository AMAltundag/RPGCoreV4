package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.npc.trait.impl.CoreCrafterTrait;
import org.bukkit.entity.Player;

public class CrafterMenu {

    public CrafterMenu() {
    }

    public void present(Player player, CoreCrafterTrait trait) {
        new me.blutkrone.rpgcore.menu.CrafterMenu(trait).finish(player);
    }
}
