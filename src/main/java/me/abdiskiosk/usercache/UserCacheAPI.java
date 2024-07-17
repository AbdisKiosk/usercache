package me.abdiskiosk.usercache;

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
import java.util.Collection;
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

    private String getTexture(Player player) {
        try {
            // Step 1: Obtain the CraftPlayer class and cast the player to CraftPlayer using reflection
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);

            // Step 2: Access the getProfile method of CraftPlayer
            Method getProfileMethod = craftPlayerClass.getDeclaredMethod("getProfile");
            getProfileMethod.setAccessible(true);

            // Step 3: Invoke getProfile to retrieve the GameProfile
            Object gameProfile = getProfileMethod.invoke(craftPlayer);

            // Step 4: Access the properties map from the GameProfile
            Method getPropertiesMethod = gameProfile.getClass().getDeclaredMethod("getProperties");
            getPropertiesMethod.setAccessible(true);
            Object properties = getPropertiesMethod.invoke(gameProfile);

            // Assuming properties is a Guava Table or similar collection that has a get method
            Method getMethod = properties.getClass().getMethod("get", Object.class, Object.class);
            getMethod.setAccessible(true);
            Object texturesProperty = getMethod.invoke(properties, "textures", "value");

            // Step 5 & 6: Check if textures property is present and return its value
            if (texturesProperty != null) {
                // Assuming texturesProperty is a Collection of Property objects
                Object texture = ((Collection<?>) texturesProperty).stream().findFirst().orElse(null);
                if (texture != null) {
                    // Assuming Property has a getValue method to get the texture value
                    Method getValueMethod = texture.getClass().getDeclaredMethod("getValue");
                    getValueMethod.setAccessible(true);
                    return (String) getValueMethod.invoke(texture);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return User.NULL_SKIN;
    }

    private String getServerVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

}