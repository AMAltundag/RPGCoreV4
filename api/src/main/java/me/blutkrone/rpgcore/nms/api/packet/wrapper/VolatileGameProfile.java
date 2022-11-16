package me.blutkrone.rpgcore.nms.api.packet.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A wrapper for a GameProfile, which will be converted by the
 * relevant volatile handler implementation.
 */
public class VolatileGameProfile {

    private final UUID id;
    private final String name;
    private final Map<String, VolatileProperty> properties;

    public VolatileGameProfile(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.properties = new HashMap<>();
    }

    public VolatileGameProfile() {
        this.id = UUID.randomUUID();
        this.name = this.id.toString().replace("-", "").substring(0, 16);
        this.properties = new HashMap<>();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, VolatileProperty> getProperties() {
        return properties;
    }

    public void addProperty(String name, String value, String signature) {
        this.properties.put(name, new VolatileProperty(name, value, signature));
    }

    public static class VolatileProperty {
        private final String name;
        private final String value;
        private final String signature;

        public VolatileProperty(String name, String value, String signature) {
            this.name = name;
            this.value = value;
            this.signature = signature;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getSignature() {
            return signature;
        }
    }
}
