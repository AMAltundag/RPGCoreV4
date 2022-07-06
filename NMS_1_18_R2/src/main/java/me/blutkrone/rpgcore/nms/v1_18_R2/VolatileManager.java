package me.blutkrone.rpgcore.nms.v1_18_R2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blutkrone.rpgcore.nms.api.*;
import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.nms.v1_18_R2.entity.VolatileEntityCollider;
import me.blutkrone.rpgcore.nms.v1_18_R2.entity.VolatileVisualEntity;
import me.blutkrone.rpgcore.nms.v1_18_R2.menu.VolatileChestMenu;
import me.blutkrone.rpgcore.nms.v1_18_R2.menu.VolatileTextInput;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.*;
import net.md_5.bungee.chat.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.BossBattleServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public final class VolatileManager extends AbstractVolatileManager implements Listener {

    private static final Gson gson = new GsonBuilder().
            registerTypeAdapter(BaseComponent.class, new ComponentSerializer()).
            registerTypeAdapter(TextComponent.class, new TextComponentSerializer()).
            registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer()).
            registerTypeAdapter(KeybindComponent.class, new KeybindComponentSerializer()).
            registerTypeAdapter(ScoreComponent.class, new ScoreComponentSerializer()).
            registerTypeAdapter(SelectorComponent.class, new SelectorComponentSerializer()).
            registerTypeAdapter(Entity.class, new EntitySerializer()).
            registerTypeAdapter(Text.class, new TextSerializer()).
            registerTypeAdapter(Item.class, new ItemSerializer()).
            registerTypeAdapter(ItemTag.class, new ItemTag.Serializer()).
            create();

    /**
     * A manager dedicated to managing volatile code behaviour.
     *
     * @param plugin the plugin which requested the manager.
     */
    public VolatileManager(JavaPlugin plugin) {
        super(plugin);

        // handle event delegation based on nms logic
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // a ticker which is fired while a volatile chest menu is opened
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Inventory inventory = player.getOpenInventory().getTopInventory();
                if (inventory instanceof VolatileChestMenu) {
                    ((VolatileChestMenu) inventory).tick();
                } else if (inventory instanceof VolatileTextInput.BukkitAnvil) {
                    ((VolatileTextInput) ((VolatileTextInput.BukkitAnvil) inventory).getVolatile()).tick();
                }
            }
        }, 1, 1);
    }

    @Override
    public IEntityVisual createVisualEntity(Location where, boolean small) {
        World world = where.getWorld();
        if (world instanceof CraftWorld) {
            VolatileVisualEntity visual = new VolatileVisualEntity(((CraftWorld) world).getHandle(), small);
            visual.move(where.getX(), where.getY(), where.getZ());
            if (!((CraftWorld) world).getHandle().addFreshEntity(visual, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
                throw new IllegalArgumentException("Could not add visual entity to server!");
            }
            return visual;
        }

        throw new IllegalArgumentException("Bad location passed");
    }

    @Override
    public IEntityCollider createCollider(org.bukkit.entity.Entity owner) {
        World world = owner.getWorld();
        if (world instanceof CraftWorld) {
            VolatileEntityCollider collider = new VolatileEntityCollider(((CraftWorld) owner.getWorld()).getHandle());
            collider.move(owner.getLocation());
            if (!((CraftWorld) world).getHandle().addFreshEntity(collider, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
                throw new IllegalArgumentException("Could not add visual entity to server!");
            }
            return collider;
        }

        throw new IllegalArgumentException("Bad location passed");
    }

    @Override
    public void updateBossBar(BossBar bar, BaseComponent[] message) {
        BossBattleServer handle = ((CraftBossBar) bar).getHandle();
        String json = ComponentSerializer.toString(message);
        handle.a(CraftChatMessage.fromJSON(json));
    }

    @Override
    public ITextInput createInput(Player holder) {
        return new VolatileTextInput(getPlugin(), holder);
    }

    @Override
    public IChestMenu createMenu(int size, Player player) {
        return new VolatileChestMenu(getPlugin(), size * 9, player);
    }

    @Override
    public String toChatString(BaseComponent[] raw) {
        String bukkit_to_json = gson.toJson(raw);
        IChatBaseComponent component = CraftChatMessage.fromJSON(bukkit_to_json);
        return CraftChatMessage.toJSON(component);
    }

    @Override
    public void setItemLore(ItemStack item, List<BaseComponent[]> lore) {
        // reference: CraftMetaItem#setDisplayTag
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        // translate the lore into appropriate NBT information
        NBTTagList tagList = new NBTTagList();
        for (BaseComponent[] line : lore) {
            tagList.add(NBTTagString.a(toChatString(line)));
        }

        // retrieve the root compound of the item
        NBTTagCompound root = nmsStack.u();
        // create or fetch 'display' compound
        NBTTagCompound display = root.p("display");
        if (!root.e("display")) {
            root.a("display", display);
        }
        // write the generated lore compound on the item
        display.a("Lore", tagList);

        // transform back into a craft item and mirror the changes
        CraftItemStack craftStack = CraftItemStack.asCraftMirror(nmsStack);
        item.setItemMeta(craftStack.getItemMeta());
    }

    @Override
    public void setItemName(ItemStack item, BaseComponent[] name) {
        // reference: CraftMetaItem#setDisplayTag
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        // translate the lore into appropriate NBT information
        NBTTagString tag = NBTTagString.a(toChatString(name));

        // retrieve the root compound of the item
        NBTTagCompound root = nmsStack.u();
        // create or fetch 'display' compound
        NBTTagCompound display = root.p("display");
        if (!root.e("display")) {
            root.a("display", display);
        }
        // write the generated lore compound on the item
        display.a("Name", tag);

        // transform back into a craft item and mirror the changes
        CraftItemStack craftStack = CraftItemStack.asCraftMirror(nmsStack);
        item.setItemMeta(craftStack.getItemMeta());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onChestMenu(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof VolatileChestMenu) {
            ((VolatileChestMenu) inventory).on(event);
        } else if (inventory instanceof VolatileTextInput.BukkitAnvil) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                Bukkit.getScheduler().runTask(getPlugin(), () -> event.getWhoClicked().closeInventory());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onChestMenu(InventoryOpenEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof VolatileChestMenu) {
            ((VolatileChestMenu) inventory).on(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onChestMenu(InventoryCloseEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof VolatileChestMenu) {
            ((VolatileChestMenu) inventory).on(event);

        } else if (inventory instanceof VolatileTextInput.BukkitAnvil) {
            ((VolatileTextInput.BukkitAnvil) inventory).getVolatile().conclude();
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onChestMenu(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof VolatileChestMenu) {
            ((VolatileChestMenu) inventory).on(event);
        } else if (inventory instanceof VolatileTextInput.BukkitAnvil) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onChestMenu(InventoryCreativeEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof VolatileChestMenu) {
            ((VolatileChestMenu) inventory).on(event);
        } else if (inventory instanceof VolatileTextInput.BukkitAnvil) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onCollide(ProjectileHitEvent e) {
        // check if we are a collider entity
        UUID uuid = VolatileEntityCollider.getLinkedEntity(e.getHitEntity());
        if (uuid == null) {
            return;
        }
        e.setCancelled(true);
        // retrieve the entity backing up
        org.bukkit.entity.Entity linked_entity = Bukkit.getEntity(uuid);
        if (linked_entity == null) {
            e.getHitEntity().remove();
            return;
        }
        // delegate the event to who we are sync-ed to
        ProjectileHitEvent event = new ProjectileHitEvent(e.getEntity(), linked_entity, e.getHitBlock());
        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onCollide(PlayerInteractEntityEvent e) {
        // check if we are a collider entity
        UUID uuid = VolatileEntityCollider.getLinkedEntity(e.getRightClicked());
        if (uuid == null) {
            return;
        }
        e.setCancelled(true);
        // retrieve the entity backing up
        org.bukkit.entity.Entity linked_entity = Bukkit.getEntity(uuid);
        if (linked_entity == null) {
            e.getRightClicked().remove();
            return;
        }
        // delegate the event to who we are sync-ed to
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(e.getPlayer(), linked_entity, e.getHand());
        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onCollide(EntityDamageEvent e) {
        // check if we are a collider entity
        UUID uuid = VolatileEntityCollider.getLinkedEntity(e.getEntity());
        if (uuid == null) {
            return;
        }
        e.setCancelled(true);
        // retrieve the entity backing up
        org.bukkit.entity.Entity linked_entity = Bukkit.getEntity(uuid);
        if (linked_entity == null) {
            e.getEntity().remove();
            return;
        }
        e.setCancelled(true);
        // delegate based on exact event
        Event event;
        if (e instanceof EntityDamageByBlockEvent) {
            Block source = ((EntityDamageByBlockEvent) e).getDamager();
            event = new EntityDamageByBlockEvent(source, linked_entity, e.getCause(), e.getDamage());
        } else if (e instanceof EntityDamageByEntityEvent) {
            org.bukkit.entity.Entity source = ((EntityDamageByEntityEvent) e).getDamager();
            event = new EntityDamageByEntityEvent(source, linked_entity, e.getCause(), e.getDamage());
        } else {
            event = new EntityDamageEvent(linked_entity, e.getCause(), e.getDamage());
        }
        Bukkit.getPluginManager().callEvent(event);
    }
}
