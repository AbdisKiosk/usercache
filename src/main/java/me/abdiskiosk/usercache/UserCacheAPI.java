package me.abdiskiosk.usercache;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
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

    public static void init(DataSource dataSource) throws SQLException {
        instance = new UserCacheAPI();

        instance.inMemory = new InMemStore();
        instance.mySQL = new MySQLStore(dataSource);
        instance.cache = new CachedStore(instance.inMemory, instance.mySQL);
    }

    public void updateCache(UUID uuid) {
        mySQL.get(uuid).ifPresent(user -> inMemory.update(user.getUuid(), user.getUsername(), user.getSkinTexture()));
    }

    public static @NotNull String getTexture(@NotNull UUID uuid) {
        return getOrDefault(uuid).getSkinTexture();
    }

    public static @NotNull String getName(@NotNull UUID uuid) {
        return getOrDefault(uuid).getUsername();
    }
    public static @NotNull User getOrDefault(@NotNull UUID uuid) {
        return getInstance().getInMemory().getOrDefault(uuid);
    }

}