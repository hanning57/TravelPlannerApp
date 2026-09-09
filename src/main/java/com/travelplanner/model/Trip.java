package com.travelplanner.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Trip {
    private final String id;
    private String name;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private final List<Activity> activities = new ArrayList<>();

    // Primary constructor (loaded from database)
    public Trip(String id, String name, String destination, LocalDate startDate, LocalDate endDate) {
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Convenience constructor (new trips created from UI)
    public Trip(String name, String destination, LocalDate startDate, LocalDate endDate) {
        this(UUID.randomUUID().toString(), name, destination, startDate, endDate);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public List<Activity> getActivities() { return activities; }

    public void addActivity(Activity activity) {
        activity.setTrip(this);
        this.activities.add(activity);
    }

    /**
     * Checks for activities that overlap with the specified date and time window.
     * 
     * @param date Target date
     * @param start Start time
     * @param end End time
     * @param excludeActivityId Activity ID to ignore (useful when editing an existing activity)
     * @return The first conflicting Activity found, or null if there is no conflict.
     */
    public Activity findConflictingActivity(LocalDate date, LocalTime start, LocalTime end, String excludeActivityId) {
        if (date == null || start == null || end == null) return null;

        for (Activity existing : activities) {
            // Ignore the activity being edited so it doesn't conflict with itself
            if (excludeActivityId != null && excludeActivityId.equals(existing.getId())) {
                continue;
            }

            // Ignore cancelled activities
            if ("Cancelled".equalsIgnoreCase(existing.getStatus())) {
                continue;
            }

            // Compare only if on the exact same date
            if (date.equals(existing.getDate())) {
                LocalTime existingStart = existing.getStartTime();
                LocalTime existingEnd = existing.getEndTime();

                if (existingStart != null && existingEnd != null) {
                    // Overlap rule: start < existingEnd AND end > existingStart
                    boolean overlaps = start.isBefore(existingEnd) && end.isAfter(existingStart);
                    if (overlaps) {
                        return existing;
                    }
                }
            }
        }
        return null;
    }
}