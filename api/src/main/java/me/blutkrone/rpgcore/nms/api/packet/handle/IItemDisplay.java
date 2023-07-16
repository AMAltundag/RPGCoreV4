package me.blutkrone.rpgcore.nms.api.packet.handle;

import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileBillboard;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileDisplay;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IItemDisplay {

    void item(Player player, ItemStack item, double scale, VolatileBillboard billboard, VolatileDisplay display);

    int getEntityId();

    void spawn(Player player, Location where);

    void spawn(Player player, double x, double y, double z);

    void destroy(Player player);

    void teleport(Player player, Location where);

    void teleport(Player player, double x, double y, double z);

    void mount(Player player, LivingEntity mount);

    void mount(Player player, int mount);
}
