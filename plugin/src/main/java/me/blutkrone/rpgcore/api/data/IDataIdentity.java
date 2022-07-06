package me.blutkrone.rpgcore.api.data;

import java.util.UUID;

/**
 * Used to identify which character a set of data belongs to, with
 * the ID -1 belonging to the roster.
 */
public interface IDataIdentity {
    static IDataIdentity of(UUID uuid, int character) {
        return new SimpleDataIdentity(uuid, character);
    }

    UUID getUserId();

    int getCharacter();

}
