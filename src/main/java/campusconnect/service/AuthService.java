package campusconnect.service;

import campusconnect.db.DataStore;
import campusconnect.db.JsonDatabase;
import campusconnect.model.Role;
import campusconnect.model.User;
import java.util.Optional;

public class AuthService {
    private final JsonDatabase db;

    public AuthService(JsonDatabase db) {
        this.db = db;
    }

    public Optional<User> login(String username) {
        DataStore s = db.load();
        return s.getUsers().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public User register(String username, Role role) {
        DataStore s = db.load();
        boolean exists = s.getUsers().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) throw new IllegalArgumentException("username already exists");
        long id = db.nextUserId(s);
        User user = new User(id, username, role);
        s.getUsers().add(user);
        db.save(s);
        return user;
    }
}
