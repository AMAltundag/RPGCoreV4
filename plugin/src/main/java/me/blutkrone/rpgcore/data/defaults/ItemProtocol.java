package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class ItemProtocol implements DataProtocol {

    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        bundle.addString(BukkitSerialization.toBase64(player.getEntity().getInventory().getContents()));
        bundle.addNumber(player.getEquipped().size());
        player.getEquipped().forEach((slot, item) -> {
            bundle.addString(slot);
            bundle.addString(BukkitSerialization.toBase64(item));
        });
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        if (!bundle.isEmpty()) {
            // retrieve the vanilla inventory
            Player entity = player.getEntity();
            try {
                ItemStack[] inventory = BukkitSerialization.fromBase64(bundle.getString(0));
                for (ItemStack item : inventory) {
                    if (item != null && RPGCore.inst().getHUDManager().getEquipMenu().isReflected(item)) {
                        item.setType(Material.AIR);
                    }

                    RPGCore.inst().getItemManager().describe(item, player);
                }
                entity.getInventory().setContents(inventory);
            } catch (IOException e) {
                entity.getInventory().setContents(new ItemStack[0]);
                e.printStackTrace();
            }

            // retrieve the core equipment
            int length = bundle.getNumber(1).intValue();
            int header = 2;
            for (int i = 0; i < length; i++) {
                String slot = bundle.getString(header++);
                String item = bundle.getString(header++);
                try {
                    ItemStack stack = BukkitSerialization.fromBase64(item)[0];
                    if (stack != null && RPGCore.inst().getHUDManager().getEquipMenu().isReflected(stack)) {
                        stack.setType(Material.AIR);
                    }

                    RPGCore.inst().getItemManager().describe(stack, player);
                    player.setEquipped(slot, stack);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // force an empty reflect call for basic setup
        RPGCore.inst().getHUDManager().getEquipMenu().applyEquipChange(player, true);
    }
}
