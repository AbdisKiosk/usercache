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

import java.lang.reflect.Method;
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
        instance.cache = new CachedStore(instance.inMemory, instance.mySQL);

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
        Method getProfileMethod = playerClass.getMethod("getProfile");
        getProfileMethod.setAccessible(true); // Make the method accessible
        Object gameProfile = getProfileMethod.invoke(player);

        Method getPropertiesMethod = gameProfile.getClass().getMethod("getProperties");
        getPropertiesMethod.setAccessible(true); // Make the method accessible
        Object properties = getPropertiesMethod.invoke(gameProfile);

        Method getMethod = properties.getClass().getMethod("get", Object.class);
        getMethod.setAccessible(true); // Make the method accessible
        Object textures = getMethod.invoke(properties, "textures");

        Method iteratorMethod = textures.getClass().getMethod("iterator");
        iteratorMethod.setAccessible(true); // Make the method accessible
        Object iterator = iteratorMethod.invoke(textures);

        Method nextMethod = iterator.getClass().getMethod("next");
        nextMethod.setAccessible(true); // Make the method accessible
        Object property = nextMethod.invoke(iterator);

        Method getValueMethod = property.getClass().getMethod("getValue");
        getValueMethod.setAccessible(true); // Make the method accessible
        String texture = (String) getValueMethod.invoke(property);

        if(texture == null) {
            return User.NULL_SKIN;
        }

        return texture;
    }

    private String getServerVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

}