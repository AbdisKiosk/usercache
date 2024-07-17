package me.abdiskiosk.usercache;

import lombok.Getter;
import lombok.SneakyThrows;
import me.abdiskiosk.usercache.cache.User;
import me.abdiskiosk.usercache.config.UserCacheConfig;
import me.abdiskiosk.usercache.store.CachedStore;
import me.abdiskiosk.usercache.store.InMemStore;
import me.abdiskiosk.usercache.store.MySQLStore;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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

    public static void init(UserCacheConfig config, Plugin plugin) throws SQLException {
        instance = new UserCacheAPI();

        instance.inMemory = new InMemStore();
        instance.mySQL = new MySQLStore(config);
        instance.cache = new CachedStore(instance.inMemory, instance.mySQL, config.getMemCacheSize());

        plugin.getServer().getPluginManager().registerEvents(new UserCacheEventListener(instance, plugin), plugin);
    }

    public void update(Player player) {
        UUID uuid = player.getUniqueId();
        String username = player.getName();
        String texture = getTexture(player);

        cache.update(uuid, username, texture);
    }

    @SneakyThrows
    private String getTexture(Player player) {
        Class<?> playerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
        Object gameProfile = playerClass.getMethod("getProfile").invoke(player);

        Object properties = gameProfile.getClass().getMethod("getProperties").invoke(gameProfile);
        Object textures = properties.getClass().getMethod("get", Object.class).invoke(properties, "textures");
        Object iterator = textures.getClass().getMethod("iterator").invoke(textures);
        Object property = iterator.getClass().getMethod("next").invoke(iterator);

        String texture = (String) property.getClass().getMethod("getValue").invoke(property);

        if(texture == null) {
            return User.NULL_SKIN;
        }

        return texture;
    }

    private String getServerVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

}