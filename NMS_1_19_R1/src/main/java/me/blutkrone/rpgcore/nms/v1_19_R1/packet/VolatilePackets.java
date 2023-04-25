package me.blutkrone.rpgcore.nms.v1_19_R1.packet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.blutkrone.rpgcore.nms.api.packet.IDispatchPacket;
import me.blutkrone.rpgcore.nms.api.packet.IVolatilePackets;
import me.blutkrone.rpgcore.nms.api.packet.handle.IBlockMutator;
import me.blutkrone.rpgcore.nms.api.packet.handle.IHighlight;
import me.blutkrone.rpgcore.nms.api.packet.handle.IHologram;
import me.blutkrone.rpgcore.nms.api.packet.handle.IPlayerNPC;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.IRegistry;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.monster.EntityMagmaCube;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static me.blutkrone.rpgcore.nms.v1_19_R1.packet.VolatilePacketSerializer.DataWatcher.*;

public class VolatilePackets implements IVolatilePackets {
    private static final EntityTypes<EntityArmorStand> ENTITY_TYPE_ARMOR_STAND = EntityTypes.d;
    private static final EntityTypes<EntityMagmaCube> ENTITY_TYPE_MAGMA_CUBE = EntityTypes.aa;
    private static final VolatilePacketSerializer.DataWatcher<Byte> STATUS = new VolatilePacketSerializer.DataWatcher<>(0, BYTE_SERIALIZER);
    private static final VolatilePacketSerializer.DataWatcher<Optional<IChatBaseComponent>> CUSTOM_NAME = new VolatilePacketSerializer.DataWatcher<>(2, OPTIONAL_CHAT_COMPONENT_SERIALIZER);
    private static final VolatilePacketSerializer.DataWatcher<Boolean> CUSTOM_NAME_VISIBILITY = new VolatilePacketSerializer.DataWatcher<>(3, BOOLEAN_SERIALIZER);
    private static final VolatilePacketSerializer.DataWatcher<EntityPose> POSE = new VolatilePacketSerializer.DataWatcher<>(6, POSE_SERIALIZER);
    private static final VolatilePacketSerializer.DataWatcher<Byte> ARMOR_STAND = new VolatilePacketSerializer.DataWatcher<>(15, BYTE_SERIALIZER);
    private static final VolatilePacketSerializer.DataWatcher<Integer> SLIME_SIZE = new VolatilePacketSerializer.DataWatcher<>(16, INT_SERIALIZER);
    private static final VolatilePacketSerializer.DataWatcher<Byte> SKIN = new VolatilePacketSerializer.DataWatcher<>(17, BYTE_SERIALIZER);
    private static int ID_TRACKER = 600_000_000;

    /*
     * Wrapper method to dispatch a packet.
     *
     * @param player who will receive the packet
     * @param packet what packet to dispatch
     */
    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().b.a(packet);
    }

    @Override
    public IBlockMutator blocks(World world, int x, int y, int z) {
        return new VolatileBlockMutator(world, x, y, z);
    }

    @Override
    public IHologram hologram() {
        return new VolatileHologram();
    }

    @Override
    public IPlayerNPC npc(UUID uuid) {
        return new VolatilePlayerNPC(uuid);
    }

    @Override
    public IHighlight highlight(int x, int y, int z) {
        return new VolatileHighlight(x, y, z);
    }

    /*
     * Convenient wrapper that allows the calling source to decide
     * on when a packet will be dispatched.
     */
    private class DispatchPacket implements IDispatchPacket {

        private final Packet[] packets;

        public DispatchPacket(Packet... packets) {
            this.packets = packets;
        }

        @Override
        public void sendTo(Player... players) {
            for (Packet packet : this.packets) {
                for (Player player : players) {
                    sendPacket(player, packet);
                }
            }
        }
    }

    private class VolatileHighlight implements IHighlight {

        private final int id;
        private final UUID uuid;
        private final int x;
        private final int y;
        private final int z;

        public VolatileHighlight(int x, int y, int z) {
            this.id = VolatilePackets.ID_TRACKER++;
            this.uuid = UUID.randomUUID();
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void enable(Player player) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeUUID(this.uuid); // uuid
            serializer.writeVarInt(IRegistry.X.a(ENTITY_TYPE_MAGMA_CUBE)); // type
            serializer.writeDouble(x + 0.5);// position
            serializer.writeDouble(y); // position
            serializer.writeDouble(z + 0.5); // position
            serializer.writeRotation(0f); // body rotation
            serializer.writeRotation(0f); // body rotation
            serializer.writeInt(0); // data
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            sendPacket(player, new PacketPlayOutSpawnEntity(serializer));

            serializer.clear();
            serializer.writeVarInt(this.id); // id
            serializer.writeDataWatcherEntry(STATUS, (byte) (0x20 | 0x40)); // invisible + glowing
            serializer.writeDataWatcherEntry(SLIME_SIZE, 2); // size 2 = block
            serializer.writeByte(0xFF); // data watcher end
            sendPacket(player, new PacketPlayOutEntityMetadata(serializer));
        }

        @Override
        public void disable(Player player) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarIntArray(this.id); // id
            sendPacket(player, new PacketPlayOutEntityDestroy(serializer));
        }
    }

    private class VolatileBlockMutator implements IBlockMutator {
        VolatilePacketSerializer serializer;
        List<Long> values;
        World world;
        Vector absolute;

        VolatileBlockMutator(World world, int x, int y, int z) {
            this.serializer = new VolatilePacketSerializer();
            this.serializer.writeSectionPosition(x, y, z); // chunk section
            this.serializer.writeBoolean(false); // light update

            this.world = world;
            this.absolute = new Vector(x << 4, y << 4, z << 4);
            this.values = new ArrayList<>();
        }

        @Override
        public Material mutate(int x, int y, int z, Material material) {
            // prepare block data which we can translate
            BlockData data = this.world.getBlockData(absolute.getBlockX() + x, absolute.getBlockY() + y, absolute.getBlockZ() + z);
            if (material != null) {
                BlockData template = data;
                data = Bukkit.createBlockData(material, created -> created.merge(template));
            }
            // prepare the two segments we will write thorough
            int block_part = net.minecraft.world.level.block.Block.i(((CraftBlockData) data).getState());
            int where_part = (short) (x << 8 | z << 4 | y);
            // write into our buffer
            this.values.add((long) (block_part << 12 | where_part));
            // offer up the transformed type
            return data.getMaterial();
        }

        @Override
        public void dispatch(Player... players) {
            // packet is deleted after dispatch
            if (this.serializer == null) {
                throw new IllegalStateException("Packet already dispatched!");
            }
            // dump block changes into serializer
            this.serializer.writeVarInt(this.values.size());
            this.values.forEach(serializer::writeVarLong);
            // dispatch the packet to the relevant parties
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(this.serializer);
            for (Player player : players) {
                sendPacket(player, packet);
            }
            // clean up the packet for memory reasons
            this.serializer = null;
        }
    }

    private class VolatileHologram implements IHologram {

        private int id;
        private UUID uuid;

        public VolatileHologram() {
            this.id = ID_TRACKER++;
            this.uuid = UUID.randomUUID();
        }

        @Override
        public int getEntityId() {
            return this.id;
        }

        @Override
        public void spawn(Player player, Location where) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeUUID(this.uuid); // uuid
            serializer.writeVarInt(IRegistry.X.a(ENTITY_TYPE_ARMOR_STAND)); // type
            serializer.writeDouble(where.getX());// position
            serializer.writeDouble(where.getY()); // position
            serializer.writeDouble(where.getZ()); // position
            serializer.writeRotation(where.getPitch()); // body rotation
            serializer.writeRotation(where.getYaw()); // body rotation
            serializer.writeInt(0); // data
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            sendPacket(player, new PacketPlayOutSpawnEntity(serializer));

            serializer.clear();
            serializer.writeVarInt(this.id); // id
            serializer.writeDataWatcherEntry(ARMOR_STAND, (byte) (0x01 | 0x02 | 0x08 | 0x10)); // hologram config
            serializer.writeDataWatcherEntry(STATUS, (byte) 0x20); // invisible
            serializer.writeByte(0xFF); // data watcher end
            sendPacket(player, new PacketPlayOutEntityMetadata(serializer));
        }

        @Override
        public void spawn(Player player, double x, double y, double z) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeUUID(this.uuid); // uuid
            serializer.writeVarInt(IRegistry.X.a(ENTITY_TYPE_ARMOR_STAND)); // type
            serializer.writeDouble(x);// position
            serializer.writeDouble(y); // position
            serializer.writeDouble(z); // position
            serializer.writeRotation(0); // body rotation
            serializer.writeRotation(0); // body rotation
            serializer.writeInt(0); // data
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            sendPacket(player, new PacketPlayOutSpawnEntity(serializer));

            serializer.clear();
            serializer.writeVarInt(this.id); // id
            serializer.writeDataWatcherEntry(ARMOR_STAND, (byte) (0x01 | 0x02 | 0x08 | 0x10)); // hologram config
            serializer.writeDataWatcherEntry(STATUS, (byte) 0x20); // invisible
            serializer.writeByte(0xFF); // data watcher end
            sendPacket(player, new PacketPlayOutEntityMetadata(serializer));
        }

        @Override
        public void destroy(Player player) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarIntArray(this.id); // id
            sendPacket(player, new PacketPlayOutEntityDestroy(serializer));
        }

        @Override
        public void equip(Player player, EquipmentSlot slot, ItemStack item) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeByte(slot.ordinal() | -128); // position | endofarray
            serializer.writeItem(item); // item
            sendPacket(player, new PacketPlayOutEntityEquipment(serializer));
        }

        @Override
        public void name(Player player, BaseComponent[] name) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            if (name.length != 0) {
                String json = ComponentSerializer.toString(name);
                IChatBaseComponent component = CraftChatMessage.fromJSON(json);
                serializer.writeDataWatcherEntry(CUSTOM_NAME, Optional.ofNullable(component)); // name to use
                serializer.writeDataWatcherEntry(CUSTOM_NAME_VISIBILITY, true); // name visible
            } else {
                serializer.writeDataWatcherEntry(CUSTOM_NAME, Optional.empty()); // name to use
                serializer.writeDataWatcherEntry(CUSTOM_NAME_VISIBILITY, false); // name visible
            }
            serializer.writeByte(0xFF); // data watcher end
            sendPacket(player, new PacketPlayOutEntityMetadata(serializer));
        }

        @Override
        public void teleport(Player player, Location where) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeDouble(where.getX()); // position
            serializer.writeDouble(where.getY()); // position
            serializer.writeDouble(where.getZ()); // position
            serializer.writeRotation(where.getPitch()); // rotation
            serializer.writeRotation(where.getYaw()); // rotation
            serializer.writeBoolean(false); // grounded
            sendPacket(player, new PacketPlayOutEntityTeleport(serializer));
        }

        @Override
        public void teleport(Player player, double x, double y, double z) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeDouble(x); // position
            serializer.writeDouble(y); // position
            serializer.writeDouble(z); // position
            serializer.writeRotation(0f); // rotation
            serializer.writeRotation(0f); // rotation
            serializer.writeBoolean(false); // grounded
            sendPacket(player, new PacketPlayOutEntityTeleport(serializer));
        }

        @Override
        public void rotate(Player player, float yaw) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeRotation(yaw); // rotation
            sendPacket(player, new PacketPlayOutEntityHeadRotation(serializer));
        }

        @Override
        public void mount(Player player, LivingEntity mount) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(mount.getEntityId()); // mount
            serializer.writeVarIntArray(this.id); // passengers
            sendPacket(player, new PacketPlayOutMount(serializer));
        }

        @Override
        public void mount(Player player, int mount) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(mount); // mount
            serializer.writeVarIntArray(this.id); // passengers
            sendPacket(player, new PacketPlayOutMount(serializer));
        }
    }

    private class VolatilePlayerNPC implements IPlayerNPC {

        private final int id;
        private final UUID uuid;

        public VolatilePlayerNPC(UUID uuid) {
            this.id = ID_TRACKER++;
            this.uuid = uuid;
        }

        @Override
        public int getEntityId() {
            return this.id;
        }

        @Override
        public IDispatchPacket animation(VolatileAnimation animation) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // entity id
            serializer.writeByte(animation.getId()); // animation id
            return new DispatchPacket(new PacketPlayOutAnimation(serializer));
        }

        @Override
        public IDispatchPacket equipment(EquipmentSlot slot, ItemStack item) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeByte(slot.ordinal() | -128); // position | endofarray
            serializer.writeItem(item); // item
            return new DispatchPacket(new PacketPlayOutEntityEquipment(serializer));
        }

        @Override
        public IDispatchPacket status(VolatileStatus... status) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            byte merge = 0x0;
            for (VolatileStatus part : status) {
                merge = (byte) (merge | part.getId());
            }
            serializer.writeDataWatcherEntry(STATUS, merge); // status
            serializer.writeByte(0xFF); // data watcher end
            return new DispatchPacket(new PacketPlayOutEntityMetadata(serializer));
        }

        @Override
        public IDispatchPacket pose(Pose pose) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeDataWatcherEntry(POSE, EntityPose.values()[pose.ordinal()]); // pose
            serializer.writeByte(0xFF); // data watcher end
            return new DispatchPacket(new PacketPlayOutEntityMetadata(serializer));
        }

        @Override
        public IDispatchPacket skin(VolatileSkin... layers) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            byte merge = 0x0;
            for (VolatileSkin layer : layers) {
                merge = (byte) (merge | layer.getCode());
            }
            serializer.writeDataWatcherEntry(SKIN, merge); // skin
            serializer.writeByte(0xFF); // data watcher end
            return new DispatchPacket(new PacketPlayOutEntityMetadata(serializer));
        }

        @Override
        public IDispatchPacket look(float pitch, float yaw) {
            Packet[] packets = new Packet[2];
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeRotation(yaw); // rotation
            packets[0] = new PacketPlayOutEntityHeadRotation(serializer);
            serializer.clear();
            serializer.writeVarInt(this.id); // id
            serializer.writeRotation(yaw); // rotation
            serializer.writeRotation(pitch); // rotation
            serializer.writeBoolean(false); // grounded
            packets[1] = PacketPlayOutEntity.PacketPlayOutEntityLook.b(serializer);
            return new DispatchPacket(packets);
        }

        @Override
        public IDispatchPacket teleport(Location where, boolean grounded) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeDouble(where.getX()); // position
            serializer.writeDouble(where.getY()); // position
            serializer.writeDouble(where.getZ()); // position
            serializer.writeRotation(where.getPitch()); // rotation
            serializer.writeRotation(where.getYaw()); // rotation
            serializer.writeBoolean(grounded); // grounded
            return new DispatchPacket(new PacketPlayOutEntityTeleport(serializer));
        }

        @Override
        public IDispatchPacket info(VolatileInfoAction action, VolatileGameProfile profile) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();

            // track the action we are adding with
            PacketPlayOutPlayerInfo.EnumPlayerInfoAction real_action = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.values()[action.ordinal()];
            serializer.writeEnum(real_action);

            // transform a ProtocolLib profile into a runtime profile
            GameProfile real_profile = new GameProfile(profile.getId(), profile.getName());
            profile.getProperties().forEach((key, property) -> {
                String name = property.getName();
                String value = property.getValue();
                String signature = property.getSignature();
                real_profile.getProperties().put(key, new Property(name, value, signature));
            });

            // packet receiver will register following players
            List<PacketPlayOutPlayerInfo.PlayerInfoData> data = new ArrayList<>();
            data.add(new PacketPlayOutPlayerInfo.PlayerInfoData(real_profile, 20, EnumGamemode.b, CraftChatMessage.fromStringOrNull(""), null));
            if (real_action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a) {
                // add new player
                serializer.writeCollection(data, datum -> {
                    serializer.writeGameProfile(datum.a());
                    serializer.writeVarInt(datum.c().a()); // gamemode of player
                    serializer.writeVarInt(datum.b()); // latency
                    serializer.writeOptionalMessage(datum.d()); // displayname of player
                    serializer.writeOptionalPublicKey(datum.e()); // public key
                });
            } else if (real_action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.b) {
                // gamemode has changed
                serializer.writeCollection(data, datum -> {
                    serializer.writeUUID(datum.a().getId()); // uuid of player
                    serializer.writeVarInt(datum.c().a()); // gamemode we changed to
                });
            } else if (real_action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.c) {
                // latency update
                serializer.writeCollection(data, datum -> {
                    serializer.writeUUID(datum.a().getId()); // uuid of player
                    serializer.writeVarInt(datum.b()); // latency of player
                });
            } else if (real_action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.d) {
                // displayname update
                serializer.writeCollection(data, datum -> {
                    serializer.writeUUID(datum.a().getId()); // uuid of player
                    serializer.writeOptionalMessage(datum.d()); // updated name
                });
            } else if (real_action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e) {
                // remove player
                serializer.writeCollection(data, datum -> {
                    serializer.writeUUID(datum.a().getId()); // uuid of player
                });
            } else {
                throw new IllegalArgumentException("Unable to resolve action " + action);
            }

            return new DispatchPacket(new PacketPlayOutPlayerInfo(serializer));
        }

        @Override
        public IDispatchPacket spawn(Location where) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeUUID(this.uuid); // uuid
            serializer.writeDouble(where.getX());// position
            serializer.writeDouble(where.getY()); // position
            serializer.writeDouble(where.getZ()); // position
            serializer.writeRotation(where.getYaw()); // body rotation
            serializer.writeRotation(where.getPitch()); // body rotation
            return new DispatchPacket(new PacketPlayOutNamedEntitySpawn(serializer));
        }

        @Override
        public IDispatchPacket destroy() {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarIntArray(this.id); // id
            return new DispatchPacket(new PacketPlayOutEntityDestroy(serializer));
        }
    }
}
