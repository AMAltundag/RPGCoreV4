package me.blutkrone.rpgcore.nms.v1_20_R1.packet;

import me.blutkrone.rpgcore.nms.api.packet.IDispatchPacket;
import me.blutkrone.rpgcore.nms.api.packet.IVolatilePackets;
import me.blutkrone.rpgcore.nms.api.packet.handle.*;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.util.*;

public class VolatilePackets implements IVolatilePackets {

    private static final VolatilePacketSerializer.EntityData<Byte> ENTITY_STATUS = new VolatilePacketSerializer.EntityData<>(0, EntityDataSerializers.BYTE);
    private static final VolatilePacketSerializer.EntityData<Pose> ENTITY_POSE = new VolatilePacketSerializer.EntityData<>(6, EntityDataSerializers.POSE);
    private static final VolatilePacketSerializer.EntityData<Integer> SLIME_SIZE = new VolatilePacketSerializer.EntityData<>(16, EntityDataSerializers.INT);
    private static final VolatilePacketSerializer.EntityData<Byte> PLAYER_SKIN_PARTS = new VolatilePacketSerializer.EntityData<>(17, EntityDataSerializers.BYTE);

    private static final VolatilePacketSerializer.EntityData<Vector3f> DISPLAY_SCALE = new VolatilePacketSerializer.EntityData<>(11, EntityDataSerializers.VECTOR3);
    private static final VolatilePacketSerializer.EntityData<Byte> DISPLAY_BILLBOARD = new VolatilePacketSerializer.EntityData<>(14, EntityDataSerializers.BYTE);
    private static final VolatilePacketSerializer.EntityData<Byte> DISPLAY_GLOW = new VolatilePacketSerializer.EntityData<>(21, EntityDataSerializers.BYTE);
    private static final VolatilePacketSerializer.EntityData<net.minecraft.world.item.ItemStack> DISPLAY_ITEM_MODEL = new VolatilePacketSerializer.EntityData<>(22, EntityDataSerializers.ITEM_STACK);
    private static final VolatilePacketSerializer.EntityData<Byte> DISPLAY_ITEM_RENDER = new VolatilePacketSerializer.EntityData<>(23, EntityDataSerializers.BYTE);
    private static final VolatilePacketSerializer.EntityData<Component> DISPLAY_TEXT_MESSAGE = new VolatilePacketSerializer.EntityData<>(22, EntityDataSerializers.COMPONENT);
    private static final VolatilePacketSerializer.EntityData<Integer> DISPLAY_TEXT_LINEBREAK = new VolatilePacketSerializer.EntityData<>(23, EntityDataSerializers.INT);
    private static final VolatilePacketSerializer.EntityData<Integer> DISPLAY_TEXT_BACKGROUND = new VolatilePacketSerializer.EntityData<>(24, EntityDataSerializers.INT);
    private static final VolatilePacketSerializer.EntityData<Byte> DISPLAY_TEXT_FORMAT = new VolatilePacketSerializer.EntityData<>(26, EntityDataSerializers.BYTE);

    private static int ID_TRACKER = 600_000_000;

    public VolatilePackets() {
    }

    @Override
    public IBlockMutator blocks(World world, int x, int y, int z) {
        return new VolatileBlockMutator(world, x, y, z);
    }

    @Override
    public ITextDisplay text() {
        return new VolatileText();
    }

    @Override
    public IItemDisplay item() {
        return new VolatileItem();
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
     * Wrapper method to dispatch a packet.
     *
     * @param player who will receive the packet
     * @param packet what packet to dispatch
     */
    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    /*
     * Convenient wrapper that allows the calling source to decide
     * on when a packet will be dispatched.
     */
    private class DispatchPacket implements IDispatchPacket {

        private final Packet<?>[] packets;

        public DispatchPacket(Packet<?>... packets) {
            this.packets = packets;
        }

        @Override
        public void sendTo(Player... players) {
            for (Packet<?> packet : this.packets) {
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
            serializer.writeId(BuiltInRegistries.ENTITY_TYPE, EntityType.MAGMA_CUBE); // type
            serializer.writeDouble(x + 0.5);// position
            serializer.writeDouble(y); // position
            serializer.writeDouble(z + 0.5); // position
            serializer.writeRotation(0f); // body rotation
            serializer.writeRotation(0f); // body rotation
            serializer.writeInt(0); // data
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            sendPacket(player, new ClientboundAddEntityPacket(serializer));

            serializer.clear();
            serializer.writeVarInt(this.id); // id
            serializer.writeEntityData(VolatilePackets.ENTITY_STATUS, (byte) (0x20 | 0x40)); // invisible + glowing
            serializer.writeEntityData(VolatilePackets.SLIME_SIZE, 2); // size 2 = block
            serializer.writeByte(0xFF); // data watcher end
            sendPacket(player, new ClientboundSetEntityDataPacket(serializer));
        }

        @Override
        public void disable(Player player) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarIntArray(new int[]{this.id}); // id
            sendPacket(player, new ClientboundRemoveEntitiesPacket(serializer));
        }
    }

    private class VolatileBlockMutator implements IBlockMutator {
        private VolatilePacketSerializer serializer;
        private List<Long> values;
        private World world;
        private Vector absolute;

        public VolatileBlockMutator(World world, int x, int y, int z) {
            this.serializer = new VolatilePacketSerializer();
            this.serializer.writeChunkPos(x, y, z); // chunk section
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
            int block_part = net.minecraft.world.level.block.Block.getId(((CraftBlockData) data).getState());
            int where_part = (short) (x << 8 | z << 4 | y);
            // write into our buffer
            this.values.add(((long) block_part) << 12 | where_part);
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
            ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(this.serializer);
            for (Player player : players) {
                sendPacket(player, packet);
            }
            // clean up the packet for memory reasons
            this.serializer = null;
        }
    }

    private class VolatileItem implements IItemDisplay {
        private int id;
        private UUID uuid;

        public VolatileItem() {
            this.id = ID_TRACKER++;
            this.uuid = UUID.randomUUID();
        }

        @Override
        public int getEntityId() {
            return this.id;
        }

        @Override
        public void item(Player player, ItemStack item, double scale, VolatileBillboard billboard, VolatileDisplay display) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeEntityData(DISPLAY_ITEM_MODEL, CraftItemStack.asNMSCopy(item)); // what item to show
            serializer.writeEntityData(DISPLAY_SCALE, new Vector3f((float) scale)); // what item to show
            serializer.writeEntityData(DISPLAY_BILLBOARD, (byte) billboard.ordinal()); // what item to show
            serializer.writeEntityData(DISPLAY_ITEM_RENDER, (byte) display.ordinal()); // what item to show
            serializer.writeByte(0xFF); // data watcher end
            sendPacket(player, new ClientboundSetEntityDataPacket(serializer));
        }

        @Override
        public void spawn(Player player, Location where) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeUUID(this.uuid); // uuid
            serializer.writeId(BuiltInRegistries.ENTITY_TYPE, EntityType.ITEM_DISPLAY); // type
            serializer.writeDouble(where.getX()); // position
            serializer.writeDouble(where.getY()); // position
            serializer.writeDouble(where.getZ()); // position
            serializer.writeRotation(where.getPitch()); // body rotation
            serializer.writeRotation(where.getYaw()); // body rotation
            serializer.writeInt(0); // data
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            sendPacket(player, new ClientboundAddEntityPacket(serializer));
        }

        @Override
        public void spawn(Player player, double x, double y, double z) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeUUID(this.uuid); // uuid
            serializer.writeId(BuiltInRegistries.ENTITY_TYPE, EntityType.ITEM_DISPLAY); // type
            serializer.writeDouble(x);// position
            serializer.writeDouble(y); // position
            serializer.writeDouble(z); // position
            serializer.writeRotation(0); // body rotation
            serializer.writeRotation(0); // body rotation
            serializer.writeInt(0); // data
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            sendPacket(player, new ClientboundAddEntityPacket(serializer));
        }

        @Override
        public void destroy(Player player) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarIntArray(new int[]{this.id}); // ids
            sendPacket(player, new ClientboundRemoveEntitiesPacket(serializer));
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
            sendPacket(player, new ClientboundTeleportEntityPacket(serializer));
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
            sendPacket(player, new ClientboundTeleportEntityPacket(serializer));
        }

        @Override
        public void mount(Player player, LivingEntity mount) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(mount.getEntityId()); // mount id
            serializer.writeVarIntArray(new int[]{this.id}); // passengers
            sendPacket(player, new ClientboundSetPassengersPacket(serializer));
        }

        @Override
        public void mount(Player player, int mount) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(mount); // mount id
            serializer.writeVarIntArray(new int[]{this.id}); // passengers
            sendPacket(player, new ClientboundSetPassengersPacket(serializer));
        }
    }

    private class VolatileText implements ITextDisplay {
        private int id;
        private UUID uuid;

        public VolatileText() {
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
            serializer.writeId(BuiltInRegistries.ENTITY_TYPE, EntityType.TEXT_DISPLAY); // type
            serializer.writeDouble(where.getX());// position
            serializer.writeDouble(where.getY()); // position
            serializer.writeDouble(where.getZ()); // position
            serializer.writeRotation(where.getPitch()); // body rotation
            serializer.writeRotation(where.getYaw()); // body rotation
            serializer.writeInt(0); // data
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            sendPacket(player, new ClientboundAddEntityPacket(serializer));
        }

        @Override
        public void spawn(Player player, double x, double y, double z, float pitch, float yaw) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeUUID(this.uuid); // uuid
            serializer.writeId(BuiltInRegistries.ENTITY_TYPE, EntityType.TEXT_DISPLAY); // type
            serializer.writeDouble(x);// position
            serializer.writeDouble(y); // position
            serializer.writeDouble(z); // position
            serializer.writeRotation(pitch); // body rotation
            serializer.writeRotation(yaw); // body rotation
            serializer.writeInt(0); // data
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            serializer.writeShort(0); // velocity
            sendPacket(player, new ClientboundAddEntityPacket(serializer));
        }

        @Override
        public void destroy(Player player) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarIntArray(new int[]{this.id}); // ids
            sendPacket(player, new ClientboundRemoveEntitiesPacket(serializer));
        }

        @Override
        public void message(Player player, BaseComponent[] message, boolean shadow, boolean locked) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeEntityData(DISPLAY_TEXT_MESSAGE, CraftChatMessage.fromJSON(ComponentSerializer.toString(message))); // message to render

            serializer.writeEntityData(DISPLAY_TEXT_FORMAT, (byte) (0x01));
            serializer.writeEntityData(DISPLAY_TEXT_BACKGROUND, 0);
            serializer.writeEntityData(DISPLAY_TEXT_LINEBREAK, 99999);
            serializer.writeEntityData(DISPLAY_BILLBOARD, (byte) (locked ? 0 : 1));

            serializer.writeByte(0xFF); // data watcher end
            sendPacket(player, new ClientboundSetEntityDataPacket(serializer));
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
            sendPacket(player, new ClientboundTeleportEntityPacket(serializer));
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
            sendPacket(player, new ClientboundTeleportEntityPacket(serializer));
        }

        @Override
        public void rotate(Player player, float yaw) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeRotation(yaw); // rotation
            sendPacket(player, new ClientboundRotateHeadPacket(serializer));
        }

        @Override
        public void mount(Player player, LivingEntity mount) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(mount.getEntityId()); // mount id
            serializer.writeVarIntArray(new int[]{this.id}); // passengers
            sendPacket(player, new ClientboundSetPassengersPacket(serializer));
        }

        @Override
        public void mount(Player player, int mount) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(mount); // mount id
            serializer.writeVarIntArray(new int[]{this.id}); // passengers
            sendPacket(player, new ClientboundSetPassengersPacket(serializer));
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
            return new DispatchPacket(new ClientboundAnimatePacket(serializer));
        }

        @Override
        public IDispatchPacket equipment(EquipmentSlot slot, ItemStack item) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeByte(slot.ordinal()); // position | endofarray
            serializer.writeItem(item); // item
            return new DispatchPacket(new ClientboundSetEquipmentPacket(serializer));
        }

        @Override
        public IDispatchPacket status(VolatileStatus... status) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            byte merge = 0x0;
            for (VolatileStatus part : status) {
                merge = (byte) (merge | part.getId());
            }
            serializer.writeEntityData(ENTITY_STATUS, merge); // status
            serializer.writeByte(0xFF); // data watcher end
            return new DispatchPacket(new ClientboundSetEntityDataPacket(serializer));
        }

        @Override
        public IDispatchPacket pose(org.bukkit.entity.Pose pose) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeEntityData(ENTITY_POSE, Pose.values()[pose.ordinal()]); // pose
            serializer.writeByte(0xFF); // data watcher end
            return new DispatchPacket(new ClientboundSetEntityDataPacket(serializer));
        }

        @Override
        public IDispatchPacket skin(VolatileSkin... layers) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            byte merge = 0x0;
            for (VolatileSkin layer : layers) {
                merge = (byte) (merge | layer.getCode());
            }
            serializer.writeEntityData(PLAYER_SKIN_PARTS, merge); // skin
            serializer.writeByte(0xFF); // data watcher end
            return new DispatchPacket(new ClientboundSetEntityDataPacket(serializer));
        }

        @Override
        public IDispatchPacket look(float pitch, float yaw) {
            Packet[] packets = new Packet[2];
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarInt(this.id); // id
            serializer.writeRotation(yaw); // rotation
            packets[0] = new ClientboundRotateHeadPacket(serializer);
            serializer.clear();
            serializer.writeVarInt(this.id); // id
            serializer.writeRotation(yaw); // rotation
            serializer.writeRotation(pitch); // rotation
            serializer.writeBoolean(false); // grounded
            packets[1] = ClientboundMoveEntityPacket.Rot.read(serializer);
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
            return new DispatchPacket(new ClientboundTeleportEntityPacket(serializer));
        }

        @Override
        public IDispatchPacket info(VolatileInfoAction action, VolatileGameProfile profile) {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();

            // process within context deemed appropriate
            if (Objects.requireNonNull(action) == VolatileInfoAction.ADD_PLAYER) {
                serializer.writeEnumSet(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), ClientboundPlayerInfoUpdatePacket.Action.class); // actions to deploy
                serializer.writeVarInt(1); // how many players
                serializer.writeUUID(profile.getId()); // uuid
                serializer.writeUtf(profile.getName()); // name
                serializer.writeGameProfileProperties(profile.getProperties()); // properties
            } else if (action == VolatileInfoAction.REMOVE_PLAYER) {
                serializer.writeEnumSet(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED), ClientboundPlayerInfoUpdatePacket.Action.class);
                serializer.writeVarInt(1); // how many players
                serializer.writeUUID(profile.getId()); // uuid
                serializer.writeBoolean(false);
            } else {
                throw new IllegalArgumentException("Unsupported profile action: " + action);
            }

            return new DispatchPacket(new ClientboundPlayerInfoUpdatePacket(serializer));
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
            return new DispatchPacket(new ClientboundAddPlayerPacket(serializer));
        }

        @Override
        public IDispatchPacket destroy() {
            VolatilePacketSerializer serializer = new VolatilePacketSerializer();
            serializer.writeVarIntArray(new int[]{this.id}); // id
            return new DispatchPacket(new ClientboundRemoveEntitiesPacket(serializer));
        }
    }
}
