package me.abdiskiosk.usercache.store;

import me.abdiskiosk.usercache.cache.User;

import java.util.Optional;
import java.util.UUID;

public interface DataStore {

    default User getOrDefault(UUID uuid) {
        return this.get(uuid).orElse(User.createDefault(uuid));
    }

    Optional<User> get(UUID uuid);
    Optional<User> get(String username);

    void update(UUID uuid, String username, String skinTexture);
}