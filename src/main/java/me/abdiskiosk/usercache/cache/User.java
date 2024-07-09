package me.abdiskiosk.usercache.cache;

import lombok.Getter;

import java.util.UUID;

@Getter
public class User {

    public static final String NULL_SKIN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZhYmY1ZDM3MDc1N2FhZjQ2ZmVmOWExOGI1MmVkYzk1OTQ1NzZjMDBhMzUzMWUwNDQ4ZTRkY2ExN2RiZjRlNCJ9fX0=";

    private final UUID uuid;
    private final String username;
    private final String skinTexture;

    public User(UUID uuid, String username, String skinTexture) {
        this.uuid = uuid;
        this.username = username;
        this.skinTexture = skinTexture;
    }

    public static User createDefault(UUID uuid) {
        return new User(uuid, "null", NULL_SKIN);
    }

}
