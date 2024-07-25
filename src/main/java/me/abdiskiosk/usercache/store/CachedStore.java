package me.abdiskiosk.usercache.store;

import me.abdiskiosk.usercache.cache.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class CachedStore implements DataStore {

    private final DataStore inMem;
    private final MySQLStore persistent;

    public CachedStore(DataStore inMem, MySQLStore persistent) {
        this.inMem = inMem;
        this.persistent = persistent;

        long start = System.currentTimeMillis();
        List<User> users = persistent.fetchAll();
        long end = System.currentTimeMillis();

        Logger.getGlobal().info("Fetched " + users.size() + " usercache from the database in " + (end - start) + "ms.");

        users.forEach(user -> inMem.update(user.getUuid(), user.getUsername(), user.getSkinTexture()));
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