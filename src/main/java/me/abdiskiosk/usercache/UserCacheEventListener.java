package me.abdiskiosk.usercache;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class UserCacheEventListener implements Listener {

    private final UserCacheAPI userCacheAPI;
    private final Plugin plugin;

    public UserCacheEventListener(UserCacheAPI userCacheAPI, Plugin plugin) {
        this.userCacheAPI = userCacheAPI;
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                userCacheAPI.update(event.getPlayer())
        );
    }

}