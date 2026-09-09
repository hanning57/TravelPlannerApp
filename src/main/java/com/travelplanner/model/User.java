package com.travelplanner.model;

import java.util.Objects;

/**
 * User model representing authentication credentials and traveler profile details.
 */
public class User {
    private final String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role; // e.g., "Traveler", "Trip Admin"

    // Authentication-only constructor
    public User(String username, String passwordHash) {
        this(username, passwordHash, username, username.toLowerCase() + "@example.com", "Traveler");
    }

    // Full profile constructor
    public User(String username, String passwordHash, String fullName, String email, String role) {
        this.username = Objects.requireNonNull(username, "Username cannot be null").trim();
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.fullName = (fullName != null && !fullName.isBlank()) ? fullName.trim() : username;
        this.email = (email != null && !email.isBlank()) ? email.trim() : "";
        this.role = (role != null && !role.isBlank()) ? role.trim() : "Traveler";
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getInitials() {
        if (fullName == null || fullName.isBlank()) return "U";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    public boolean passwordMatches(String passwordPlain, java.util.function.Function<String, String> hasher) {
        if (passwordPlain == null) return false;
        return passwordHash.equals(hasher.apply(passwordPlain));
    }

    public String toCsv() {
        return String.join(",", escapeCsv(username), escapeCsv(passwordHash), 
                           escapeCsv(fullName), escapeCsv(email), escapeCsv(role));
    }

    public static User fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("CSV record cannot be empty");
        }
        String[] parts = line.split(",", -1);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid user record: insufficient fields");
        }
        String username = parts[0];
        String passwordHash = parts[1];
        String fullName = parts.length > 2 && !parts[2].isBlank() ? parts[2] : username;
        String email = parts.length > 3 ? parts[3] : "";
        String role = parts.length > 4 ? parts[4] : "Traveler";

        return new User(username, passwordHash, fullName, email, role);
    }

    private static String escapeCsv(String value) {
        return value == null ? "" : value.replace(",", ";");
    }
}