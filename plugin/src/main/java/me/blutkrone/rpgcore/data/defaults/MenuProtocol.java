package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Map;

public class MenuProtocol  implements DataProtocol {
    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        Map<String, ItemStack> persistence = player.getMenuPersistence();
        bundle.addNumber(persistence.size());
        persistence.forEach((id, data) -> {
            bundle.addString(id);
            bundle.addString(BukkitSerialization.toBase64(data));
        });
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle) {
        if (!bundle.isEmpty()) {
            // retrieve the persistence
            int length = bundle.getNumber(0).intValue();
            int header = 1;
            for (int i = 0; i < length; i++) {
                String id = bundle.getString(header++);
                String items = bundle.getString(header++);
                try {
                    player.getMenuPersistence().put(id, BukkitSerialization.fromBase64(items)[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

