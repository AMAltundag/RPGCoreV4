package me.blutkrone.rpgcore.nms.v1_19_R3.packet;

import io.netty.buffer.Unpooled;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileGameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.Mth;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class VolatilePacketSerializer extends FriendlyByteBuf {

    public VolatilePacketSerializer() {
        super(Unpooled.buffer());
    }

    public void writeChunkPos(int x, int y, int z) {
        long var = 0L;
        var |= ((long) x & 0x3fffffL) << 42;
        var |= ((long) y & 0xfffffL);
        var |= ((long) z & 0x3fffffL) << 20;
        super.writeLong(var);
    }

    public void writeRotation(float rotation) {
        writeByte(Mth.floor(rotation * 256.0F / 360.0F));
    }

    public <K> void writeEntityData(EntityData<K> key, K value) {
        writeByte(key.getIndex());
        writeVarInt(key.getSerializerTypeID());
        key.getSerializer().write(this, value);
    }

    public void writeItem(ItemStack item) {
        writeItem(CraftItemStack.asNMSCopy(item));
    }

    public void writeGameProfile(VolatileGameProfile profile) {
        super.writeUUID(profile.getId());
        super.writeUtf(profile.getName());
        this.writeGameProfileProperties(profile.getProperties());
    }

    public void writeGameProfileProperties(Map<String, VolatileGameProfile.VolatileProperty> properties) {
        super.writeVarInt(properties.size());
        for (VolatileGameProfile.VolatileProperty property : properties.values()) {
            writeProperty(property);
        }
    }

    public void writeProperty(VolatileGameProfile.VolatileProperty property) {
        this.writeUtf(property.getName());
        this.writeUtf(property.getValue());
        if (property.getSignature() != null) {
            this.writeBoolean(true);
            this.writeUtf(property.getSignature());
        } else {
            this.writeBoolean(false);
        }
    }

    public static class EntityData<K> {

        private final int index;
        private final EntityDataSerializer<K> serializer;
        private final int serializer_type_id;

        /**
         * Convenient wrapper to ease up on data watcher manipulation.
         *
         * @param index      position within entity data
         * @param serializer type to serialize with
         */
        public EntityData(int index, EntityDataSerializer<K> serializer) {
            this.index = index;
            this.serializer = serializer;
            this.serializer_type_id = EntityDataSerializers.getSerializedId(serializer);
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
        public EntityDataSerializer<K> getSerializer() {
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
