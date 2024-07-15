package me.abdiskiosk.usercache;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import me.abdiskiosk.usercache.cache.User;
import me.abdiskiosk.usercache.config.UserCacheConfig;
import me.abdiskiosk.usercache.store.CachedStore;
import me.abdiskiosk.usercache.store.InMemStore;
import me.abdiskiosk.usercache.store.MySQLStore;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
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
        CraftPlayer craftPlayer = (CraftPlayer) player;
        GameProfile profile = craftPlayer.getProfile();

        Property texture = profile.getProperties().get("textures").stream().findFirst().orElse(null);

        if(texture == null) {
            return User.NULL_SKIN;
        }

        return texture.getValue();
    }

    private String getServerVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

}