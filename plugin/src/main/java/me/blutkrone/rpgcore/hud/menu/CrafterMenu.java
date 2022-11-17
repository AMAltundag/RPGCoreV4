package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.item.crafting.CoreCraftingRecipe;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreCrafterTrait;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrafterMenu {

    public CrafterMenu() {
    }

    public void present(Player player, CoreCrafterTrait trait) {
        new me.blutkrone.rpgcore.menu.CrafterMenu(trait).finish(player);
    }
}
