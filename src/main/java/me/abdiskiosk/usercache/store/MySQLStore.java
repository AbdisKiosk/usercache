package me.abdiskiosk.usercache.store;

import lombok.SneakyThrows;
import me.abdiskiosk.usercache.cache.User;
import me.abdiskiosk.usercache.config.UserCacheConfig;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MySQLStore implements DataStore {

    private final MariaDbDataSource dataSource;
    private Connection connection;

    public MySQLStore(UserCacheConfig config) throws SQLException {
        this.dataSource = new MariaDbDataSource(
                "jdbc:mariadb://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase()
        );

        dataSource.setUser(config.getUsername());
        dataSource.setPassword(config.getPassword());

        this.connection = dataSource.getConnection();
    }


    @SneakyThrows
    @Override
    public Optional<User> get(UUID uuid) {
        try(
                PreparedStatement stmt = connection.prepareStatement("SELECT uuid, username, skin_texture FROM user_cache WHERE uuid = ?")
        ) {
            stmt.setString(1, uuid.toString());

            return Optional.ofNullable(map(stmt.executeQuery()));
        }
    }

    @SneakyThrows
    @Override
    public Optional<User> get(String username) {
        try(
                PreparedStatement stmt = connection.prepareStatement("SELECT uuid, username, skin_texture FROM user_cache WHERE LOWER(username) = LOWER(?)")
        ) {
            stmt.setString(1, username);

            return Optional.ofNullable(map(stmt.executeQuery()));
        }
    }

    @SneakyThrows
    protected User map(ResultSet resultSet) {
        if(!resultSet.next()) {
            return null;
        }

        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        String username = resultSet.getString("username");
        String skinTexture = resultSet.getString("skin_texture");

        return new User(uuid, username, skinTexture);
    }

    @SneakyThrows
    @Override
    public void update(UUID uuid, String username, String skinTexture) {
        try(
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO user_cache (uuid, username, skin_texture, last_join) VALUES (?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE username = VALUES(username), " +
                                    "skin_texture = VALUES(skin_texture), last_join = VALUES(last_join)"
                )
        ) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, skinTexture);
            stmt.setDate(4, new Date(System.currentTimeMillis()));

            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public List<User> fetch(int size) {
        try(
                PreparedStatement stmt = connection.prepareStatement("SELECT uuid, username, skin_texture FROM user_cache ORDER BY last_join DESC LIMIT ?")
        ) {
            stmt.setInt(1, size);

            List<User> users = new ArrayList<>();

            while(true) {
                User user = map(stmt.executeQuery());
                if(user == null) {
                    break;
                }
                users.add(user);
            }

            return users;
        }
    }

}