package me.abdiskiosk.usercache.store;

import me.abdiskiosk.usercache.cache.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemStore implements DataStore {

    private final Map<UUID, User> idToUser = new HashMap<>();
    private final Map<String, User> nameToUser = new HashMap<>();

    @Override
    public synchronized Optional<User> get(UUID uuid) {
        return Optional.ofNullable(idToUser.get(uuid));
    }

    @Override
    public synchronized Optional<User> get(String username) {
        return Optional.ofNullable(nameToUser.get(username.toLowerCase()));
    }

    @Override
    public synchronized void update(UUID uuid, String username, String skinTexture) {
        User user = new User(uuid, username, skinTexture);
        idToUser.put(uuid, user);
        nameToUser.put(username.toLowerCase(), user);
    }
}
