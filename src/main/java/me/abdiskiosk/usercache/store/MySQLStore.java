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

    public MySQLStore(UserCacheConfig config) throws SQLException {
        this.dataSource = new MariaDbDataSource(
                "jdbc:mariadb://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase()
        );

        dataSource.setUser(config.getUsername());
        dataSource.setPassword(config.getPassword());
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


    @SneakyThrows
    @Override
    public Optional<User> get(UUID uuid) {
        try(
                PreparedStatement stmt = getConnection().prepareStatement("SELECT uuid, username, texture FROM users WHERE uuid = ?")
        ) {
            stmt.setString(1, uuid.toString());

            return Optional.ofNullable(map(stmt.executeQuery()));
        }
    }

    @SneakyThrows
    @Override
    public Optional<User> get(String username) {
        try(
                PreparedStatement stmt = getConnection().prepareStatement("SELECT uuid, username, texture FROM users WHERE LOWER(username) = LOWER(?)")
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
        String skinTexture = resultSet.getString("texture");

        return new User(uuid, username, skinTexture);
    }

    @SneakyThrows
    @Override
    public void update(UUID uuid, String username, String skinTexture) {
        try(
                PreparedStatement stmt = getConnection().prepareStatement(
                        "INSERT INTO users (uuid, username, texture) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE username = VALUES(username), " +
                                    "texture = VALUES(texture), username = VALUES(username)"
                )
        ) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, skinTexture);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public List<User> fetchAll() {
        try(
                PreparedStatement stmt = getConnection().prepareStatement("SELECT uuid, username, texture FROM users")
        ) {
            List<User> users = new ArrayList<>();
            ResultSet res = stmt.executeQuery();
            while(true) {
                User user = map(res);
                if(user == null) {
                    break;
                }
                users.add(user);
            }

            return users;
        }
    }

}