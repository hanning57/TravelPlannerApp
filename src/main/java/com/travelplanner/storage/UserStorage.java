package com.travelplanner.storage;

import com.travelplanner.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserStorage {

    private static final String DB_URL = "jdbc:sqlite:travelplanner.db";

    static {
        initDatabase();
    }

    private static void initDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                email TEXT,
                role TEXT DEFAULT 'Traveler'
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Seed default administrator account if empty
            if (!usernameExists("johndoe")) {
                register("johndoe", "1234", "John Doe", "john@example.com", "Traveler");
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    /**
     * Enforces single account per identity.
     * Rejects duplicate registrations at the database level.
     */
    public static synchronized User register(String username, String rawPassword, String fullName, String email, String role) {
        if (username == null || username.trim().isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }

        String sanitizedUser = username.trim().toLowerCase();
        String hashed = hashPassword(rawPassword);

        String sql = "INSERT INTO users(username, password_hash, full_name, email, role) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sanitizedUser);
            pstmt.setString(2, hashed);
            pstmt.setString(3, (fullName != null && !fullName.isBlank()) ? fullName.trim() : sanitizedUser);
            pstmt.setString(4, (email != null) ? email.trim() : "");
            pstmt.setString(5, (role != null) ? role.trim() : "Traveler");

            pstmt.executeUpdate();
            return new User(sanitizedUser, hashed, fullName, email, role);

        } catch (SQLException e) {
            // Error code 19 = SQLITE_CONSTRAINT (Unique constraint violation)
            return null;
        }
    }

    public static synchronized User register(String username, String rawPassword) {
        return register(username, rawPassword, username, username.toLowerCase() + "@example.com", "Traveler");
    }

    public static synchronized User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) return null;

        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String attemptHash = hashPassword(rawPassword);

                if (storedHash.equals(attemptHash)) {
                    return new User(
                        rs.getString("username"),
                        storedHash,
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    public static synchronized boolean usernameExists(String username) {
        if (username == null) return false;

        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public static synchronized User findByUsername(String username) {
        if (username == null) return null;

        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("User lookup error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing user's profile details and password directly in SQLite.
     */
    public static synchronized boolean updateUser(User user, String newRawPassword) {
        if (user == null || user.getUsername() == null) return false;

        String targetUsername = user.getUsername().trim().toLowerCase();
        boolean changePassword = (newRawPassword != null && !newRawPassword.isBlank());

        String sql = changePassword
                ? "UPDATE users SET full_name = ?, email = ?, role = ?, password_hash = ? WHERE LOWER(username) = ?"
                : "UPDATE users SET full_name = ?, email = ?, role = ? WHERE LOWER(username) = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getRole());

            if (changePassword) {
                String newHash = hashPassword(newRawPassword);
                user.setPasswordHash(newHash); // Update in-memory user instance
                pstmt.setString(4, newHash);
                pstmt.setString(5, targetUsername);
            } else {
                pstmt.setString(4, targetUsername);
            }

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user password/profile: " + e.getMessage());
            return false;
        }
    }

        public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(plainPassword.hashCode());
        }
    }

}
