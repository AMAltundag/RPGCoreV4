package me.blutkrone.rpgcore.nms.v1_20_R1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blutkrone.rpgcore.nms.api.AbstractVolatileManager;
import me.blutkrone.rpgcore.nms.api.block.ChunkOutline;
import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.nms.api.packet.IVolatilePackets;
import me.blutkrone.rpgcore.nms.v1_20_R1.entity.VolatileEntityCollider;
import me.blutkrone.rpgcore.nms.v1_20_R1.menu.VolatileChestMenu;
import me.blutkrone.rpgcore.nms.v1_20_R1.menu.VolatileTextInput;
import me.blutkrone.rpgcore.nms.v1_20_R1.mob.VolatileEntityBase;
import me.blutkrone.rpgcore.nms.v1_20_R1.packet.VolatilePackets;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.*;
import net.md_5.bungee.chat.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Entity;
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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class VolatileManager extends AbstractVolatileManager implements Listener {

    private static final Gson gson = new GsonBuilder().
            registerTypeAdapter(BaseComponent.class, new ComponentSerializer()).
            registerTypeAdapter(TextComponent.class, new TextComponentSerializer()).
            registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer()).
            registerTypeAdapter(KeybindComponent.class, new KeybindComponentSerializer()).
            registerTypeAdapter(ScoreComponent.class, new ScoreComponentSerializer()).
            registerTypeAdapter(SelectorComponent.class, new SelectorComponentSerializer()).
            registerTypeAdapter(net.md_5.bungee.api.chat.hover.content.Entity.class, new EntitySerializer()).
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

        Bukkit.getLogger().severe("not implemented (outline needs more accuracy)");
    }

    @Override
    public ChunkOutline getChunkOutline(Chunk chunk) {
        // retrieve NMS chunk representation
        LevelChunk chunk_nms = ((CraftWorld) chunk.getWorld()).getHandle().getChunk(chunk.getX(), chunk.getZ());
        LevelChunkSection[] sections = chunk_nms.getSections();
        // identify the outline of the chunk
        ChunkOutline outline = new ChunkOutline(chunk_nms.getHeight());
        for (int s = 0; s < sections.length; s++) {
            LevelChunkSection section = sections[s];
            if (section != null && !section.hasOnlyAir()) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            BlockState state = section.getBlockState(x, y, z);
                            outline.set(x, s*16+y, z, !state.isAir());
                        }
                    }
                }
            }
        }
        // offer up what we found
        return outline;
    }

    @Override
    public void sendMessage(BaseComponent[] message, Collection<Player> target) {
        ClientboundSystemChatPacket packet = new ClientboundSystemChatPacket(message, false);
        for (Player player : target) {
            ((CraftPlayer) player).getHandle().connection.send(packet);
        }
    }

    @Override
    public int getMajorVersion() {
        return 19;
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
        // fixme: does this still work?
        ServerBossEvent handle = ((CraftBossBar) bar).getHandle();
        String json = ComponentSerializer.toString(message);
        handle.setName(CraftChatMessage.fromJSON(json));
    }

    @Override
    public IChestMenu createMenu(int size, Player holder, Object core_handle) {
        return new VolatileChestMenu(getPlugin(), size * 9, holder, core_handle);
    }

    @Override
    public ITextInput createInput(Player holder) {
        return new VolatileTextInput(getPlugin(), holder);
    }

    @Override
    public String toChatString(BaseComponent[] raw) {
        // fixme: does this still work?
        String bukkit_to_json = gson.toJson(raw);
        Component component = CraftChatMessage.fromJSON(bukkit_to_json);
        return CraftChatMessage.toJSON(component);
    }

    @Override
    public Object adaptComponent(BaseComponent[] input) {
        // fixme: does this still work?
        String bukkit_to_json = gson.toJson(input);
        return CraftChatMessage.fromJSON(bukkit_to_json);
    }

    @Override
    public void setItemLore(ItemStack item, List<BaseComponent[]> lore) {
        // reference: CraftMetaItem#setDisplayTag
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        // translate the lore into appropriate NBT information
        ListTag tagList = new ListTag();
        for (BaseComponent[] line : lore) {
            tagList.add(StringTag.valueOf(toChatString(line)));
        }

        // retrieve the root compound of the item
        CompoundTag root = nmsStack.getOrCreateTag();
        // create or fetch 'display' compound
        CompoundTag display = root.getCompound("display");
        if (!root.contains("display")) {
            root.put("display", display);
        }
        // write the generated lore compound on the item
        display.put("Lore", tagList);

        // transform back into a craft item and mirror the changes
        CraftItemStack craftStack = CraftItemStack.asCraftMirror(nmsStack);
        item.setItemMeta(craftStack.getItemMeta());
    }

    @Override
    public void setItemName(ItemStack item, BaseComponent[] name) {
        // reference: CraftMetaItem#setDisplayTag
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        // translate the lore into appropriate NBT information
        StringTag tag = StringTag.valueOf(toChatString(name));

        // retrieve the root compound of the item
        CompoundTag root = nmsStack.getOrCreateTag();
        // create or fetch 'display' compound
        CompoundTag display = root.getCompound("display");
        if (!root.contains("display")) {
            root.put("display", display);
        }
        // write the generated lore compound on the item
        display.put("Name", tag);

        // transform back into a craft item and mirror the changes
        CraftItemStack craftStack = CraftItemStack.asCraftMirror(nmsStack);
        item.setItemMeta(craftStack.getItemMeta());
    }

    @Override
    public IEntityBase spawnEntity(EntityType type, Location location) {
        // fixme this is broken

        // ensure that our location is a valid one
        World world = location.getWorld();
        if (world == null) {
            throw new NullPointerException("World cannot be null!");
        }

        // map to a base entity type to for clean spawning
        net.minecraft.world.entity.EntityType<?> typeNMS = null;
        if (type == EntityType.AXOLOTL) {
            typeNMS = net.minecraft.world.entity.EntityType.AXOLOTL;
        } else if (type == EntityType.BAT) {
            typeNMS = net.minecraft.world.entity.EntityType.BAT;
        } else if (type == EntityType.BEE) {
            typeNMS = net.minecraft.world.entity.EntityType.BEE;
        } else if (type == EntityType.BLAZE) {
            typeNMS = net.minecraft.world.entity.EntityType.BLAZE;
        } else if (type == EntityType.CAT) {
            typeNMS = net.minecraft.world.entity.EntityType.CAT;
        } else if (type == EntityType.CAVE_SPIDER) {
            typeNMS = net.minecraft.world.entity.EntityType.CAVE_SPIDER;
        } else if (type == EntityType.CHICKEN) {
            typeNMS = net.minecraft.world.entity.EntityType.CHICKEN;
        } else if (type == EntityType.COD) {
            typeNMS = net.minecraft.world.entity.EntityType.COD;
        } else if (type == EntityType.COW) {
            typeNMS = net.minecraft.world.entity.EntityType.COW;
        } else if (type == EntityType.CREEPER) {
            typeNMS = net.minecraft.world.entity.EntityType.CREEPER;
        } else if (type == EntityType.DOLPHIN) {
            typeNMS = net.minecraft.world.entity.EntityType.DOLPHIN;
        } else if (type == EntityType.DONKEY) {
            typeNMS = net.minecraft.world.entity.EntityType.DONKEY;
        } else if (type == EntityType.DROWNED) {
            typeNMS = net.minecraft.world.entity.EntityType.DROWNED;
        } else if (type == EntityType.ELDER_GUARDIAN) {
            typeNMS = net.minecraft.world.entity.EntityType.ELDER_GUARDIAN;
        } else if (type == EntityType.ENDERMAN) {
            typeNMS = net.minecraft.world.entity.EntityType.ENDERMAN;
        } else if (type == EntityType.ENDERMITE) {
            typeNMS = net.minecraft.world.entity.EntityType.ENDERMITE;
        } else if (type == EntityType.EVOKER) {
            typeNMS = net.minecraft.world.entity.EntityType.EVOKER;
        } else if (type == EntityType.FOX) {
            typeNMS = net.minecraft.world.entity.EntityType.FOX;
        } else if (type == EntityType.GHAST) {
            typeNMS = net.minecraft.world.entity.EntityType.GHAST;
        } else if (type == EntityType.GLOW_SQUID) {
            typeNMS = net.minecraft.world.entity.EntityType.GLOW_SQUID;
        } else if (type == EntityType.GOAT) {
            typeNMS = net.minecraft.world.entity.EntityType.GOAT;
        } else if (type == EntityType.GUARDIAN) {
            typeNMS = net.minecraft.world.entity.EntityType.GUARDIAN;
        } else if (type == EntityType.HOGLIN) {
            typeNMS = net.minecraft.world.entity.EntityType.HOGLIN;
        } else if (type == EntityType.HORSE) {
            typeNMS = net.minecraft.world.entity.EntityType.HORSE;
        } else if (type == EntityType.HUSK) {
            typeNMS = net.minecraft.world.entity.EntityType.HUSK;
        } else if (type == EntityType.ILLUSIONER) {
            typeNMS = net.minecraft.world.entity.EntityType.ILLUSIONER;
        } else if (type == EntityType.IRON_GOLEM) {
            typeNMS = net.minecraft.world.entity.EntityType.IRON_GOLEM;
        } else if (type == EntityType.LLAMA) {
            typeNMS = net.minecraft.world.entity.EntityType.LLAMA;
        } else if (type == EntityType.MAGMA_CUBE) {
            typeNMS = net.minecraft.world.entity.EntityType.MAGMA_CUBE;
        } else if (type == EntityType.MULE) {
            typeNMS = net.minecraft.world.entity.EntityType.MULE;
        } else if (type == EntityType.MUSHROOM_COW) {
            typeNMS = net.minecraft.world.entity.EntityType.MOOSHROOM;
        } else if (type == EntityType.OCELOT) {
            typeNMS = net.minecraft.world.entity.EntityType.OCELOT;
        } else if (type == EntityType.PANDA) {
            typeNMS = net.minecraft.world.entity.EntityType.PANDA;
        } else if (type == EntityType.PARROT) {
            typeNMS = net.minecraft.world.entity.EntityType.PARROT;
        } else if (type == EntityType.PHANTOM) {
            typeNMS = net.minecraft.world.entity.EntityType.PHANTOM;
        } else if (type == EntityType.PIG) {
            typeNMS = net.minecraft.world.entity.EntityType.PIG;
        } else if (type == EntityType.PIGLIN) {
            typeNMS = net.minecraft.world.entity.EntityType.PIGLIN;
        } else if (type == EntityType.PIGLIN_BRUTE) {
            typeNMS = net.minecraft.world.entity.EntityType.PIGLIN_BRUTE;
        } else if (type == EntityType.PILLAGER) {
            typeNMS = net.minecraft.world.entity.EntityType.PILLAGER;
        } else if (type == EntityType.POLAR_BEAR) {
            typeNMS = net.minecraft.world.entity.EntityType.POLAR_BEAR;
        } else if (type == EntityType.PUFFERFISH) {
            typeNMS = net.minecraft.world.entity.EntityType.PUFFERFISH;
        } else if (type == EntityType.RABBIT) {
            typeNMS = net.minecraft.world.entity.EntityType.RABBIT;
        } else if (type == EntityType.RAVAGER) {
            typeNMS = net.minecraft.world.entity.EntityType.RAVAGER;
        } else if (type == EntityType.SALMON) {
            typeNMS = net.minecraft.world.entity.EntityType.SALMON;
        } else if (type == EntityType.SHEEP) {
            typeNMS = net.minecraft.world.entity.EntityType.SHEEP;
        } else if (type == EntityType.SHULKER) {
            typeNMS = net.minecraft.world.entity.EntityType.SHULKER;
        } else if (type == EntityType.SILVERFISH) {
            typeNMS = net.minecraft.world.entity.EntityType.SILVERFISH;
        } else if (type == EntityType.SKELETON) {
            typeNMS = net.minecraft.world.entity.EntityType.SKELETON;
        } else if (type == EntityType.SKELETON_HORSE) {
            typeNMS = net.minecraft.world.entity.EntityType.SKELETON_HORSE;
        } else if (type == EntityType.SLIME) {
            typeNMS = net.minecraft.world.entity.EntityType.SLIME;
        } else if (type == EntityType.SNOWMAN) {
            typeNMS = net.minecraft.world.entity.EntityType.SNOW_GOLEM;
        } else if (type == EntityType.SPIDER) {
            typeNMS = net.minecraft.world.entity.EntityType.SPIDER;
        } else if (type == EntityType.SQUID) {
            typeNMS = net.minecraft.world.entity.EntityType.SQUID;
        } else if (type == EntityType.STRAY) {
            typeNMS = net.minecraft.world.entity.EntityType.STRAY;
        } else if (type == EntityType.STRIDER) {
            typeNMS = net.minecraft.world.entity.EntityType.STRIDER;
        } else if (type == EntityType.TRADER_LLAMA) {
            typeNMS = net.minecraft.world.entity.EntityType.TRADER_LLAMA;
        } else if (type == EntityType.TROPICAL_FISH) {
            typeNMS = net.minecraft.world.entity.EntityType.TROPICAL_FISH;
        } else if (type == EntityType.TURTLE) {
            typeNMS = net.minecraft.world.entity.EntityType.TURTLE;
        } else if (type == EntityType.VEX) {
            typeNMS = net.minecraft.world.entity.EntityType.VEX;
        } else if (type == EntityType.VILLAGER) {
            typeNMS = net.minecraft.world.entity.EntityType.VILLAGER;
        } else if (type == EntityType.VINDICATOR) {
            typeNMS = net.minecraft.world.entity.EntityType.VINDICATOR;
        } else if (type == EntityType.WANDERING_TRADER) {
            typeNMS = net.minecraft.world.entity.EntityType.WANDERING_TRADER;
        } else if (type == EntityType.WITCH) {
            typeNMS = net.minecraft.world.entity.EntityType.WITCH;
        } else if (type == EntityType.WITHER) {
            typeNMS = net.minecraft.world.entity.EntityType.WITHER;
        } else if (type == EntityType.WITHER_SKELETON) {
            typeNMS = net.minecraft.world.entity.EntityType.WITHER_SKELETON;
        } else if (type == EntityType.WOLF) {
            typeNMS = net.minecraft.world.entity.EntityType.WOLF;
        } else if (type == EntityType.ZOGLIN) {
            typeNMS = net.minecraft.world.entity.EntityType.ZOGLIN;
        } else if (type == EntityType.ZOMBIE) {
            typeNMS = net.minecraft.world.entity.EntityType.ZOMBIE;
        } else if (type == EntityType.ZOMBIE_HORSE) {
            typeNMS = net.minecraft.world.entity.EntityType.ZOMBIE_HORSE;
        } else if (type == EntityType.ZOMBIE_VILLAGER) {
            typeNMS = net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER;
        } else if (type == EntityType.ZOMBIFIED_PIGLIN) {
            typeNMS = net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN;
        } else if (type == EntityType.ALLAY) {
            typeNMS = net.minecraft.world.entity.EntityType.ALLAY;
        } else if (type == EntityType.FROG) {
            typeNMS = net.minecraft.world.entity.EntityType.FROG;
        } else if (type == EntityType.TADPOLE) {
            typeNMS = net.minecraft.world.entity.EntityType.TADPOLE;
        } else if (type == EntityType.WARDEN) {
            typeNMS = net.minecraft.world.entity.EntityType.WARDEN;
        }

        // ensure we could map to a NMS type
        if (typeNMS == null) {
            getPlugin().getLogger().severe("Entity type '" + type + "' is not supported!");
            return null;
        }

        // manually instantiate the entity to force that copies are without variants
        Level server = ((CraftWorld) world).getHandle();
        net.minecraft.world.entity.Entity entity = typeNMS.create(server);
        if (entity == null) {
            throw new IllegalStateException("Bad factorized entity type " + type);
        }

        // move to an appropriate position
        entity.absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.setYHeadRot(location.getYaw());

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

    @Override
    public String getItemTagAsJSON(ItemStack item) {
        try {
            net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            CompoundTag compound = new CompoundTag();
            compound = nmsStack.save(compound);
            return compound.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "{}";
        }
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
        Entity linked_entity = Bukkit.getEntity(uuid);
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
        Entity linked_entity = Bukkit.getEntity(uuid);
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
        Entity linked_entity = Bukkit.getEntity(uuid);
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
            Entity source = ((EntityDamageByEntityEvent) e).getDamager();
            event = new EntityDamageByEntityEvent(source, linked_entity, e.getCause(), e.getDamage());
        } else {
            event = new EntityDamageEvent(linked_entity, e.getCause(), e.getDamage());
        }
        Bukkit.getPluginManager().callEvent(event);
    }
}
