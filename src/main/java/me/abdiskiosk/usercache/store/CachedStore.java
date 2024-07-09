package me.abdiskiosk.usercache.store;

import me.abdiskiosk.usercache.cache.User;

import java.util.Optional;
import java.util.UUID;

public class CachedStore implements DataStore {

    private final DataStore inMem;
    private final MySQLStore persistent;

    public CachedStore(DataStore inMem, MySQLStore persistent, int size) {
        this.inMem = inMem;
        this.persistent = persistent;

        persistent.fetch(size).forEach(user -> inMem.update(user.getUuid(), user.getUsername(), user.getSkinTexture()));
    }

    @Override
    public Optional<User> get(UUID uuid) {
        return Optional.ofNullable(inMem.get(uuid)
                .orElseGet(() -> persistent.get(uuid).orElse(null)));
    }

    @Override
    public Optional<User> get(String username) {
        return Optional.ofNullable(inMem.get(username)
                .orElseGet(() -> persistent.get(username).orElse(null)));
    }

    @Override
    public void update(UUID uuid, String username, String skinTexture) {
        inMem.update(uuid, username, skinTexture);
        persistent.update(uuid, username, skinTexture);
    }

}