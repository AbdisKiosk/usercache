package me.abdiskiosk.usercache;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
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

    public void update(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        String username = player.getName();
        String texture = getTexture(player);

        cache.update(uuid, username, texture);
    }

    private String getTexture(Player player) {
        try {
            // Get the CraftPlayer class
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");

            // Cast the player to CraftPlayer
            Object craftPlayer = craftPlayerClass.cast(player);

            // Get the getHandle method from the CraftPlayer class
            Method getHandleMethod = craftPlayerClass.getMethod("getHandle");

            // Invoke the getHandle method
            Object entityPlayer = getHandleMethod.invoke(craftPlayer);

            // Get the GameProfile using reflection
            Method getProfileMethod = entityPlayer.getClass().getMethod("getProfile");
            Object gameProfile = getProfileMethod.invoke(entityPlayer);

            // Get the properties map from the GameProfile
            Method getPropertiesMethod = gameProfile.getClass().getMethod("getProperties");
            Object properties = getPropertiesMethod.invoke(gameProfile);

            // Get the textures property from the properties map
            Method getMethod = properties.getClass().getMethod("get", Object.class);
            Object texturesProperty = getMethod.invoke(properties, "textures");

            // Get the iterator of the textures property
            Method iteratorMethod = texturesProperty.getClass().getMethod("iterator");
            Object iterator = iteratorMethod.invoke(texturesProperty);

            // Get the next property from the iterator
            Method nextMethod = iterator.getClass().getMethod("next");
            Object property = nextMethod.invoke(iterator);

            // Get the value of the property
            Method getValueMethod = property.getClass().getMethod("getValue");
            String texture = (String) getValueMethod.invoke(property);

            return texture;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return User.NULL_SKIN;
    }

    private String getServerVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

}