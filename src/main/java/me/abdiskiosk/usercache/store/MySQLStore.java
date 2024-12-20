package me.abdiskiosk.usercache.store;

import lombok.SneakyThrows;
import me.abdiskiosk.usercache.cache.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MySQLStore implements DataStore {

    private final DataSource dataSource;

    public MySQLStore(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


    @SneakyThrows
    @Override
    public Optional<User> get(UUID uuid) {
        try(
                Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement("SELECT uuid, username, texture FROM users WHERE uuid = ?")
        ) {
            stmt.setString(1, uuid.toString());

            return Optional.ofNullable(map(stmt.executeQuery()));
        }
    }

    @SneakyThrows
    @Override
    public Optional<User> get(String username) {
        try(
                Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement("SELECT uuid, username, texture FROM users WHERE LOWER(username) = LOWER(?)")
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
                Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO users (uuid, username, texture) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE username = VALUES(username), " +
                                    "texture = VALUES(texture), username = VALUES(username)"
                )
        ) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, skinTexture);

            stmt.executeUpdate();
        }
    }

    @SneakyThrows
    public List<User> fetchAll() {
        try(
                Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement("SELECT uuid, username, texture FROM users")
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