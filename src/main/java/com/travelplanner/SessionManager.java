package com.travelplanner;

import com.travelplanner.model.User;
import com.travelplanner.storage.UserStorage;

import java.io.*;
import java.nio.file.*;

public class SessionManager {

    private static final String SESSION_FILE = "session.txt";
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void saveSession(String username) {
        try {
            Files.writeString(Path.of(SESSION_FILE), username.trim());
        } catch (IOException ignored) {}
    }

    public static User loadSession() {
        try {
            Path path = Path.of(SESSION_FILE);
            if (Files.exists(path)) {
                String username = Files.readString(path).trim();
                if (!username.isEmpty()) {
                    User user = UserStorage.findByUsername(username);
                    if (user != null) {
                        currentUser = user;
                        return user;
                    }
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    public static void clearSession() {
        currentUser = null;
        try {
            Files.deleteIfExists(Path.of(SESSION_FILE));
        } catch (IOException ignored) {}
    }
}
