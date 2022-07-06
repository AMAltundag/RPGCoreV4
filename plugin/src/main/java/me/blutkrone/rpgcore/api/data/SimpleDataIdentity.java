package me.blutkrone.rpgcore.api.data;

import java.util.UUID;

public class SimpleDataIdentity implements IDataIdentity {

    private final UUID uuid;
    private final int character;

    public SimpleDataIdentity(UUID uuid, int character) {
        this.uuid = uuid;
        this.character = character;
    }

    @Override
    public UUID getUserId() {
        return this.uuid;
    }

    @Override
    public int getCharacter() {
        return this.character;
    }
}
