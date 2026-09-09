package com.travelplanner.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Activity {
    private final String id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String recurrence;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Trip trip;

    // Full constructor (for DB hydration)
    public Activity(String id, String title, LocalDate date, LocalTime startTime, LocalTime endTime,
                    String category, String priority, String recurrence, String description, String status) {
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = (category != null) ? category : "Sightseeing";
        this.priority = (priority != null) ? priority : "Medium";
        this.recurrence = (recurrence != null) ? recurrence : "None";
        this.description = (description != null) ? description : "";
        this.status = (status != null) ? status : "Planned";
    }

    // Convenience constructor (for UI creation)
    public Activity(String title, LocalDate date, LocalTime startTime, LocalTime endTime,
                    String category, String priority, String recurrence, String description) {
        this(UUID.randomUUID().toString(), title, date, startTime, endTime, category, priority, recurrence, description, "Planned");
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getRecurrence() { return recurrence; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }
}