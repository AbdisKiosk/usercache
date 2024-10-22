package me.abdiskiosk.usercache;

import lombok.Getter;
import lombok.NonNull;
import me.abdiskiosk.usercache.cache.User;
import me.abdiskiosk.usercache.store.CachedStore;
import me.abdiskiosk.usercache.store.InMemStore;
import me.abdiskiosk.usercache.store.MySQLStore;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

public class UserCacheAPI {

    @Getter
    private static UserCacheAPI instance;

    @Getter
    private InMemStore inMemory;
    @Getter
    private MySQLStore mySQL;
    @Getter
    private CachedStore cache;

    public static void init(@NonNull DataSource dataSource) throws SQLException {
        instance = new UserCacheAPI();

        instance.inMemory = new InMemStore();
        instance.mySQL = new MySQLStore(dataSource);
        instance.cache = new CachedStore(instance.inMemory, instance.mySQL);
    }

    public void updateCache(@NonNull UUID uuid) {
        mySQL.get(uuid).ifPresent(user -> inMemory.update(user.getUuid(), user.getUsername(), user.getSkinTexture()));
    }

    public static @NonNull String getTexture(@NonNull UUID uuid) {
        return getOrDefault(uuid).getSkinTexture();
    }

    public static @NonNull String getName(@NonNull UUID uuid) {
        return getOrDefault(uuid).getUsername();
    }
    public static @NonNull User getOrDefault(@NonNull UUID uuid) {
        return getInstance().getInMemory().getOrDefault(uuid);
    }

}