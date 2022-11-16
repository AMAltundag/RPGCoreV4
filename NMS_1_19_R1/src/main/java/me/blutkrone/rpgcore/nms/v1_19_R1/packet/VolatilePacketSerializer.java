package me.blutkrone.rpgcore.nms.v1_19_R1.packet;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.network.syncher.DataWatcherSerializer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/*
 * Simple serializer for packet data.
 */
public class VolatilePacketSerializer extends PacketDataSerializer {

    public VolatilePacketSerializer() {
        super(Unpooled.buffer());
    }

    public void writeVarIntArray(int... values) {
        writeVarInt(values.length);
        for (int value : values) {
            writeVarInt(value);
        }
    }

    public void writeMessage(IChatBaseComponent message) {
        super.a(message);
    }

    public void writeOptionalMessage(IChatBaseComponent message) {
        if (message == null) {
            super.writeBoolean(false);
        } else {
            super.writeBoolean(true);
            this.writeMessage(message);
        }
    }

    public void writeOptionalPublicKey(ProfilePublicKey.a key) {
        if (key == null) {
            super.writeBoolean(false);
        } else {
            super.writeBoolean(true);
            key.a(this);
        }
    }

    public void writeVarInt(int value) {
        super.d(value);
    }

    public void writeVarLong(long value) {
        super.b(value);
    }

    public void writeUUID(UUID uuid) {
        super.a(uuid);
    }

    public void writeUTF(String string) {
        super.a(string);
    }
    
    public void writeGameProfile(GameProfile profile) {
        super.a(profile);
    }

    public void writeSectionPosition(int x, int y, int z) {
        long var3 = 0L;
        var3 |= ((long) x & 0x3fffffL) << 42;
        var3 |= ((long) y & 0xfffffL);
        var3 |= ((long) z & 0x3fffffL) << 20;
        this.writeLong(var3);
    }

    public void writeRotation(float rotation) {
        writeByte(MathHelper.d(rotation * 256.0F / 360.0F));
    }

    public void writeItem(ItemStack item) {
        a(CraftItemStack.asNMSCopy(item));
    }

    public void writeEnum(Enum<?> value) {
        a(value);
    }

    public <K> void writeCollection(Collection<K> collection, Consumer<K> consumer) {
        writeVarInt(collection.size());
        for (K value : collection) {
            consumer.accept(value);
        }
    }

    public <K> void writeDataWatcherEntry(DataWatcher<K> key, K value) {
        writeByte(key.getIndex());
        writeVarInt(key.getSerializerTypeID());
        key.getSerializer().a(this, value);
    }


    /**
     * Convenient wrapper to ease up on data watcher manipulation
     *
     * @param <K>
     */
    public static class DataWatcher<K> {

        public static final DataWatcherSerializer<Byte> BYTE_SERIALIZER = DataWatcherRegistry.a;
        public static final DataWatcherSerializer<Integer> INT_SERIALIZER = DataWatcherRegistry.b;
        public static final DataWatcherSerializer<Boolean> BOOLEAN_SERIALIZER = DataWatcherRegistry.i;
        public static final DataWatcherSerializer<EntityPose> POSE_SERIALIZER = DataWatcherRegistry.t;
        public static final DataWatcherSerializer<net.minecraft.world.item.ItemStack> ITEM_STACK_SERIALIZER = DataWatcherRegistry.g;
        public static final DataWatcherSerializer<Optional<IChatBaseComponent>> OPTIONAL_CHAT_COMPONENT_SERIALIZER = DataWatcherRegistry.f;

        private final int index;
        private final DataWatcherSerializer<K> serializer;
        private final int serializer_type_id;

        /**
         * Convenient wrapper to ease up on data watcher manipulation.
         *
         * @param index      position within entity data
         * @param serializer type to serialize with
         */
        public DataWatcher(int index, DataWatcherSerializer<K> serializer) {
            this.index = index;
            this.serializer = serializer;
            this.serializer_type_id = DataWatcherRegistry.b(serializer);
            if (this.serializer_type_id < 0) {
                throw new IllegalArgumentException("Unable to resolve serializer " + serializer);
            }
        }

        /**
         * Position within data parameters.
         *
         * @return index of the data value.
         */
        public int getIndex() {
            return this.index;
        }

        /**
         * Serialization protocol for data.
         *
         * @return data serializer.
         */
        public DataWatcherSerializer<K> getSerializer() {
            return this.serializer;
        }

        /**
         * Identifier of the serialization type.
         *
         * @return data serializer ID.
         */
        public int getSerializerTypeID() {
            return this.serializer_type_id;
        }
    }
}