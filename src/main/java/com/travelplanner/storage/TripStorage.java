package com.travelplanner.storage;

import com.travelplanner.SessionManager;
import com.travelplanner.model.Activity;
import com.travelplanner.model.Trip;
import com.travelplanner.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TripStorage {

    private static final String DB_URL = "jdbc:sqlite:travelplanner.db";
    public static final List<Trip> trips = new ArrayList<>();

    static {
        initDatabase();
    }

    private static void initDatabase() {
        String createTripsTable = """
            CREATE TABLE IF NOT EXISTS trips (
                id TEXT PRIMARY KEY,
                owner_username TEXT NOT NULL,
                name TEXT NOT NULL,
                destination TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL
            );
        """;

        String createActivitiesTable = """
            CREATE TABLE IF NOT EXISTS activities (
                id TEXT PRIMARY KEY,
                trip_id TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                category TEXT,
                priority TEXT,
                recurrence TEXT,
                activity_date TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                status TEXT NOT NULL,
                FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
            );
        """;

        // Prevents deleted trips from re-appearing on next boot
        String createSeedTrackerTable = """
            CREATE TABLE IF NOT EXISTS user_seed_tracker (
                username TEXT PRIMARY KEY,
                seeded INTEGER DEFAULT 1
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createTripsTable);
            stmt.execute(createActivitiesTable);
            stmt.execute(createSeedTrackerTable);
        } catch (SQLException e) {
            System.err.println("Database table setup failed: " + e.getMessage());
        }
    }

    /**
     * Loads trips and activities belonging specifically to the logged-in user.
     */
    public static synchronized void loadUserTrips() {
        trips.clear(); // Ensure in-memory list is fresh

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        String username = currentUser.getUsername().toLowerCase();

        // 1. If user was never seeded, insert default records FIRST into database
        if (!hasUserBeenSeeded(username)) {
            seedDefaultTripsForUser(username);
            markUserAsSeeded(username);
        }

        // 2. Query ONLY from SQLite into in-memory list
        String selectTripsSql = "SELECT * FROM trips WHERE owner_username = ? ORDER BY start_date ASC";
        String selectActivitiesSql = "SELECT * FROM activities WHERE trip_id = ? ORDER BY activity_date ASC, start_time ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement tripStmt = conn.prepareStatement(selectTripsSql)) {

            tripStmt.setString(1, username);
            ResultSet tripRs = tripStmt.executeQuery();

            while (tripRs.next()) {
                String tripId = tripRs.getString("id");
                String name = tripRs.getString("name");
                String destination = tripRs.getString("destination");
                LocalDate startDate = LocalDate.parse(tripRs.getString("start_date"));
                LocalDate endDate = LocalDate.parse(tripRs.getString("end_date"));

                Trip trip = new Trip(tripId, name, destination, startDate, endDate);

                try (PreparedStatement actStmt = conn.prepareStatement(selectActivitiesSql)) {
                    actStmt.setString(1, tripId);
                    ResultSet actRs = actStmt.executeQuery();

                    while (actRs.next()) {
                        Activity act = new Activity(
                            actRs.getString("id"),
                            actRs.getString("title"),
                            LocalDate.parse(actRs.getString("activity_date")),
                            LocalTime.parse(actRs.getString("start_time")),
                            LocalTime.parse(actRs.getString("end_time")),
                            actRs.getString("category"),
                            actRs.getString("priority"),
                            actRs.getString("recurrence"),
                            actRs.getString("description"),
                            actRs.getString("status")
                        );
                        trip.addActivity(act);
                    }
                }
                trips.add(trip);
            }

        } catch (SQLException e) {
            System.err.println("Error loading trips: " + e.getMessage());
        }
    }

    /**
     * Persists all in-memory trips and nested activities for the active user.
     */
    public static synchronized void saveTrips() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        String username = currentUser.getUsername().toLowerCase();

        String deleteActivitiesSql = "DELETE FROM activities WHERE trip_id IN (SELECT id FROM trips WHERE owner_username = ?)";
        String deleteTripsSql = "DELETE FROM trips WHERE owner_username = ?";
        String insertTripSql = "INSERT INTO trips (id, owner_username, name, destination, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
        String insertActivitySql = "INSERT INTO activities (id, trip_id, title, description, category, priority, recurrence, activity_date, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement delActs = conn.prepareStatement(deleteActivitiesSql);
                 PreparedStatement delTrips = conn.prepareStatement(deleteTripsSql)) {
                delActs.setString(1, username);
                delActs.executeUpdate();
                delTrips.setString(1, username);
                delTrips.executeUpdate();
            }

            try (PreparedStatement insertTrip = conn.prepareStatement(insertTripSql);
                 PreparedStatement insertAct = conn.prepareStatement(insertActivitySql)) {

                for (Trip trip : trips) {
                    insertTrip.setString(1, trip.getId());
                    insertTrip.setString(2, username);
                    insertTrip.setString(3, trip.getName());
                    insertTrip.setString(4, trip.getDestination());
                    insertTrip.setString(5, trip.getStartDate().toString());
                    insertTrip.setString(6, trip.getEndDate().toString());
                    insertTrip.executeUpdate();

                    for (Activity act : trip.getActivities()) {
                        insertAct.setString(1, act.getId());
                        insertAct.setString(2, trip.getId());
                        insertAct.setString(3, act.getTitle());
                        insertAct.setString(4, act.getDescription() != null ? act.getDescription() : "");
                        insertAct.setString(5, act.getCategory());
                        insertAct.setString(6, act.getPriority());
                        insertAct.setString(7, act.getRecurrence());
                        insertAct.setString(8, act.getDate().toString());
                        insertAct.setString(9, act.getStartTime().toString());
                        insertAct.setString(10, act.getEndTime().toString());
                        insertAct.setString(11, act.getStatus());
                        insertAct.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error saving trips: " + e.getMessage());
        }
    }

    private static boolean hasUserBeenSeeded(String username) {
        String sql = "SELECT 1 FROM user_seed_tracker WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static void markUserAsSeeded(String username) {
        String sql = "INSERT OR IGNORE INTO user_seed_tracker (username, seeded) VALUES (?, 1)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to mark user as seeded: " + e.getMessage());
        }
    }

        private static void seedDefaultTripsForUser(String username) {
        // Clear memory list so we do not duplicate items
        trips.clear();

        Trip paris = new Trip("Paris Adventure", "Paris, France",
                              LocalDate.of(2026, 5, 14), LocalDate.of(2026, 5, 20));
        paris.addActivity(new Activity("Visit Eiffel Tower", LocalDate.of(2026, 5, 15),
                                       LocalTime.of(10, 0), LocalTime.of(12, 30),
                                       "Sightseeing", "High", "None", "Pre-booked tower ascent"));
        paris.addActivity(new Activity("Louvre Museum Tour", LocalDate.of(2026, 5, 16),
                                       LocalTime.of(14, 0), LocalTime.of(17, 0),
                                       "Museum", "Medium", "None", "Guided museum visit"));

        Trip bali = new Trip("Bali Retreat", "Bali, Indonesia",
                             LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 18));
        bali.addActivity(new Activity("Beach Day", LocalDate.of(2026, 6, 10),
                                      LocalTime.of(9, 0), LocalTime.of(16, 0),
                                      "Leisure", "Low", "None", "Relaxing on Kuta Beach"));
        bali.addActivity(new Activity("Snorkeling", LocalDate.of(2026, 6, 12),
                                      LocalTime.of(8, 30), LocalTime.of(11, 30),
                                      "Sports", "High", "None", "Coral reef discovery"));

        trips.add(paris);
        trips.add(bali);

        // Save directly to DB
        saveTrips();
        
        // Clear out the memory list so the subsequent SQL read handles hydration cleanly
        trips.clear();
    }

}