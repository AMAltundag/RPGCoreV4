package me.blutkrone.rpgcore.nms.v1_18_R2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blutkrone.rpgcore.nms.api.AbstractVolatileManager;
import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.nms.api.packet.IVolatilePackets;
import me.blutkrone.rpgcore.nms.v1_18_R2.entity.VolatileEntityCollider;
import me.blutkrone.rpgcore.nms.v1_18_R2.entity.VolatileVisualEntity;
import me.blutkrone.rpgcore.nms.v1_18_R2.menu.VolatileChestMenu;
import me.blutkrone.rpgcore.nms.v1_18_R2.menu.VolatileTextInput;
import me.blutkrone.rpgcore.nms.v1_18_R2.mob.VolatileEntityBase;
import me.blutkrone.rpgcore.nms.v1_18_R2.packet.VolatilePackets;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.*;
import net.md_5.bungee.chat.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.BossBattleServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityTypes;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
    private int entity_id = 300_000_000;

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
    public IVolatilePackets getPackets() {
        return new VolatilePackets();
    }

    @Override
    public int getNextEntityId() {
        // faster access then using reflection for the NMS tracker
        return this.entity_id++;
    }

    @Override
    public IEntityVisual createVisualEntity(Location where, boolean small) {
        Bukkit.getLogger().severe("REPLACE VISUAL ENTITIES WITH HOLOGRAMS");
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
    public IChestMenu createMenu(int size, Player player, Object core_handle) {
        return new VolatileChestMenu(getPlugin(), size * 9, player, core_handle);
    }

    @Override
    public String toChatString(BaseComponent[] raw) {
        String bukkit_to_json = gson.toJson(raw);
        IChatBaseComponent component = CraftChatMessage.fromJSON(bukkit_to_json);
        return CraftChatMessage.toJSON(component);
    }

    @Override
    public Object adaptComponent(BaseComponent[] input) {
        String bukkit_to_json = gson.toJson(input);
        return CraftChatMessage.fromJSON(bukkit_to_json);
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

    @Override
    public IEntityBase spawnEntity(EntityType type, Location location) {
        // ensure that our location is a valid one
        World world = location.getWorld();
        if (world == null) {
            throw new NullPointerException("World cannot be null!");
        }

        // map to a base entity type to for clean spawning
        EntityTypes typeNMS = null;
        if (type == EntityType.AXOLOTL) {
            typeNMS = EntityTypes.e;
        } else if (type == EntityType.BAT) {
            typeNMS = EntityTypes.f;
        } else if (type == EntityType.BEE) {
            typeNMS = EntityTypes.g;
        } else if (type == EntityType.BLAZE) {
            typeNMS = EntityTypes.h;
        } else if (type == EntityType.CAT) {
            typeNMS = EntityTypes.j;
        } else if (type == EntityType.CAVE_SPIDER) {
            typeNMS = EntityTypes.k;
        } else if (type == EntityType.CHICKEN) {
            typeNMS = EntityTypes.l;
        } else if (type == EntityType.COD) {
            typeNMS = EntityTypes.m;
        } else if (type == EntityType.COW) {
            typeNMS = EntityTypes.n;
        } else if (type == EntityType.CREEPER) {
            typeNMS = EntityTypes.o;
        } else if (type == EntityType.DOLPHIN) {
            typeNMS = EntityTypes.p;
        } else if (type == EntityType.DONKEY) {
            typeNMS = EntityTypes.q;
        } else if (type == EntityType.DROWNED) {
            typeNMS = EntityTypes.s;
        } else if (type == EntityType.ELDER_GUARDIAN) {
            typeNMS = EntityTypes.t;
        } else if (type == EntityType.ENDERMAN) {
            typeNMS = EntityTypes.w;
        } else if (type == EntityType.ENDERMITE) {
            typeNMS = EntityTypes.x;
        } else if (type == EntityType.EVOKER) {
            typeNMS = EntityTypes.y;
        } else if (type == EntityType.FOX) {
            typeNMS = EntityTypes.E;
        } else if (type == EntityType.GHAST) {
            typeNMS = EntityTypes.F;
        } else if (type == EntityType.GLOW_SQUID) {
            typeNMS = EntityTypes.I;
        } else if (type == EntityType.GOAT) {
            typeNMS = EntityTypes.J;
        } else if (type == EntityType.GUARDIAN) {
            typeNMS = EntityTypes.K;
        } else if (type == EntityType.HOGLIN) {
            typeNMS = EntityTypes.L;
        } else if (type == EntityType.HORSE) {
            typeNMS = EntityTypes.M;
        } else if (type == EntityType.HUSK) {
            typeNMS = EntityTypes.N;
        } else if (type == EntityType.ILLUSIONER) {
            typeNMS = EntityTypes.O;
        } else if (type == EntityType.IRON_GOLEM) {
            typeNMS = EntityTypes.P;
        } else if (type == EntityType.LLAMA) {
            typeNMS = EntityTypes.V;
        } else if (type == EntityType.MAGMA_CUBE) {
            typeNMS = EntityTypes.X;
        } else if (type == EntityType.MULE) {
            typeNMS = EntityTypes.ag;
        } else if (type == EntityType.MUSHROOM_COW) {
            typeNMS = EntityTypes.ah;
        } else if (type == EntityType.OCELOT) {
            typeNMS = EntityTypes.ai;
        } else if (type == EntityType.PANDA) {
            typeNMS = EntityTypes.ak;
        } else if (type == EntityType.PARROT) {
            typeNMS = EntityTypes.al;
        } else if (type == EntityType.PHANTOM) {
            typeNMS = EntityTypes.am;
        } else if (type == EntityType.PIG) {
            typeNMS = EntityTypes.an;
        } else if (type == EntityType.PIGLIN) {
            typeNMS = EntityTypes.ao;
        } else if (type == EntityType.PIGLIN_BRUTE) {
            typeNMS = EntityTypes.ap;
        } else if (type == EntityType.PILLAGER) {
            typeNMS = EntityTypes.aq;
        } else if (type == EntityType.POLAR_BEAR) {
            typeNMS = EntityTypes.ar;
        } else if (type == EntityType.PUFFERFISH) {
            typeNMS = EntityTypes.at;
        } else if (type == EntityType.RABBIT) {
            typeNMS = EntityTypes.au;
        } else if (type == EntityType.RAVAGER) {
            typeNMS = EntityTypes.av;
        } else if (type == EntityType.SALMON) {
            typeNMS = EntityTypes.aw;
        } else if (type == EntityType.SHEEP) {
            typeNMS = EntityTypes.ax;
        } else if (type == EntityType.SHULKER) {
            typeNMS = EntityTypes.ay;
        } else if (type == EntityType.SILVERFISH) {
            typeNMS = EntityTypes.aA;
        } else if (type == EntityType.SKELETON) {
            typeNMS = EntityTypes.aB;
        } else if (type == EntityType.SKELETON_HORSE) {
            typeNMS = EntityTypes.aC;
        } else if (type == EntityType.SLIME) {
            typeNMS = EntityTypes.aD;
        } else if (type == EntityType.SNOWMAN) {
            typeNMS = EntityTypes.aF;
        } else if (type == EntityType.SPIDER) {
            typeNMS = EntityTypes.aI;
        } else if (type == EntityType.SQUID) {
            typeNMS = EntityTypes.aJ;
        } else if (type == EntityType.STRAY) {
            typeNMS = EntityTypes.aK;
        } else if (type == EntityType.STRIDER) {
            typeNMS = EntityTypes.aL;
        } else if (type == EntityType.TRADER_LLAMA) {
            typeNMS = EntityTypes.aR;
        } else if (type == EntityType.TROPICAL_FISH) {
            typeNMS = EntityTypes.aS;
        } else if (type == EntityType.TURTLE) {
            typeNMS = EntityTypes.aT;
        } else if (type == EntityType.VEX) {
            typeNMS = EntityTypes.aU;
        } else if (type == EntityType.VILLAGER) {
            typeNMS = EntityTypes.aV;
        } else if (type == EntityType.VINDICATOR) {
            typeNMS = EntityTypes.aW;
        } else if (type == EntityType.WANDERING_TRADER) {
            typeNMS = EntityTypes.aX;
        } else if (type == EntityType.WITCH) {
            typeNMS = EntityTypes.aY;
        } else if (type == EntityType.WITHER) {
            typeNMS = EntityTypes.aZ;
        } else if (type == EntityType.WITHER_SKELETON) {
            typeNMS = EntityTypes.ba;
        } else if (type == EntityType.WOLF) {
            typeNMS = EntityTypes.bc;
        } else if (type == EntityType.ZOGLIN) {
            typeNMS = EntityTypes.bd;
        } else if (type == EntityType.ZOMBIE) {
            typeNMS = EntityTypes.be;
        } else if (type == EntityType.ZOMBIE_HORSE) {
            typeNMS = EntityTypes.bf;
        } else if (type == EntityType.ZOMBIE_VILLAGER) {
            typeNMS = EntityTypes.bg;
        } else if (type == EntityType.ZOMBIFIED_PIGLIN) {
            typeNMS = EntityTypes.bh;
        }

        // ensure we could map to a NMS type
        if (typeNMS == null) {
            Bukkit.getLogger().severe("Entity type '" + type + "' is not supported!");
            return null;
        }

        // manually instantiate the entity to force that copies are without variants
        WorldServer server = ((CraftWorld) world).getHandle();
        net.minecraft.world.entity.Entity entity = typeNMS.a(server);
        if (entity == null) {
            throw new IllegalStateException("Bad factorized entity type " + type);
        }

        // move to an appropriate position
        entity.a(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.l(location.getYaw());

        // register the creature that has been requested
        server.addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);

        // initialize the bukkit entity into a wrapper
        LivingEntity bukkit_entity = (LivingEntity) entity.getBukkitEntity();
        if (bukkit_entity == null) {
            throw new IllegalStateException("Could not bukkit resolve " + entity.getClass());
        }

        VolatileEntityBase entity_base = new VolatileEntityBase(bukkit_entity);
        entity_base.getAI();
        return entity_base;
    }

    @Override
    public IEntityBase getEntity(LivingEntity entity) {
        VolatileEntityBase entity_base = new VolatileEntityBase(entity);
        entity_base.getAI();
        return entity_base;
    }

    @EventHandler(priority = EventPriority.LOW)
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

    @EventHandler(priority = EventPriority.LOW)
    void onChestMenu(InventoryOpenEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof VolatileChestMenu) {
            ((VolatileChestMenu) inventory).on(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    void onChestMenu(InventoryCloseEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof VolatileChestMenu) {
            ((VolatileChestMenu) inventory).on(event);

        } else if (inventory instanceof VolatileTextInput.BukkitAnvil) {
            ((VolatileTextInput.BukkitAnvil) inventory).getVolatile().conclude();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    void onChestMenu(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory instanceof VolatileChestMenu) {
            ((VolatileChestMenu) inventory).on(event);
        } else if (inventory instanceof VolatileTextInput.BukkitAnvil) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
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
