package me.abdiskiosk.usercache.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class UserCacheConfig {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String database;

    private final int memCacheSize;

    @Override
    public String toString() {
        return "UserCacheConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + "(hidden)" + '\'' +
                ", database='" + database + '\'' +
                ", memCacheSize=" + memCacheSize +
                '}';
    }
}