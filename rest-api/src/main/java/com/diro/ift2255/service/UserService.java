package com.diro.ift2255.service;

import com.diro.ift2255.model.User;
import java.util.*;

public class UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    public UserService() {
        // Mock users
        users.put(nextId, new User(nextId++, "Alice", "alice@example.com"));
        users.put(nextId, new User(nextId++, "Bob", "bob@example.com"));
    }

    /** Fetch all users */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }


    /** Fetch a user by ID */
    public Optional<User> getUserById(int id) {
        try {
            User user = users.get(id);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Create (add) a user */
    public void createUser(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
    }

    /** Update a user */
    public void updateUser(int id, User updated) {
        updated.setId(id);
        users.put(id, updated);
    }

    /** Delete a user */
    public void deleteUser(int id) {
        users.remove(id);
    }
}
