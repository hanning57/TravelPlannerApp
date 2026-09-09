package com.travelplanner.ui;

import com.travelplanner.SceneManager;
import com.travelplanner.model.Activity;
import com.travelplanner.model.Trip;
import com.travelplanner.storage.TripStorage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class ActivityEditScreen extends BorderPane {

    private final Activity activity;

    public ActivityEditScreen(Activity activity) {
        this.activity = activity;
        this.setStyle("-fx-background-color: #F8F9FA;");
        buildUI();
    }

    private void buildUI() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setMaxWidth(680);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #EAEAEA; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);"
        );

        Label titleLbl = new Label("Edit Activity Details");
        titleLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Trip parentTrip = activity.getTrip();
        String tripInfo = parentTrip != null 
                ? parentTrip.getName() + " (" + parentTrip.getStartDate() + " to " + parentTrip.getEndDate() + ")"
                : "Unassigned";

        Label idBadge = new Label("ID: " + activity.getId() + "  |  Trip: " + tripInfo);
        idBadge.setStyle("-fx-text-fill: #6B7280; -fx-font-family: monospace; -fx-font-size: 11px;");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        TextField titleField = new TextField(activity.getTitle());
        TextArea descArea = new TextArea(activity.getDescription() != null ? activity.getDescription() : "");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Sightseeing", "Food", "Leisure", "Transport", "Museum", "Sports");
        catBox.setValue(activity.getCategory() != null ? activity.getCategory() : "Sightseeing");

        ComboBox<String> prioBox = new ComboBox<>();
        prioBox.getItems().addAll("Low", "Medium", "High");
        prioBox.setValue(activity.getPriority() != null ? activity.getPriority() : "Medium");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Planned", "In Progress", "Completed", "Cancelled");
        statusBox.setValue(activity.getStatus() != null ? activity.getStatus() : "Planned");

        DatePicker datePicker = new DatePicker(activity.getDate());

        // Constrain date selection strictly within the trip's start and end dates
        if (parentTrip != null && parentTrip.getStartDate() != null && parentTrip.getEndDate() != null) {
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) return;
                    if (item.isBefore(parentTrip.getStartDate()) || item.isAfter(parentTrip.getEndDate())) {
                        setDisable(true);
                        setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #9CA3AF;");
                    }
                }
            });
        }

        TextField startTimeField = new TextField(activity.getStartTime() != null ? activity.getStartTime().toString() : "10:00");
        TextField endTimeField = new TextField(activity.getEndTime() != null ? activity.getEndTime().toString() : "12:00");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catBox, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(prioBox, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusBox, 1, 3);
        grid.add(new Label("Date:"), 0, 4);
        grid.add(datePicker, 1, 4);
        grid.add(new Label("Start Time (HH:mm):"), 0, 5);
        grid.add(startTimeField, 1, 5);
        grid.add(new Label("End Time (HH:mm):"), 0, 6);
        grid.add(endTimeField, 1, 6);
        grid.add(new Label("Description:"), 0, 7);
        grid.add(descArea, 1, 7);

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #0B5FFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            if (titleField.getText().trim().isBlank()) {
                showError("Title cannot be empty.");
                return;
            }

            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate == null) {
                showError("Please pick a valid date.");
                return;
            }

            // Measurement 1: Validate activity falls within trip period
            if (parentTrip != null) {
                if (selectedDate.isBefore(parentTrip.getStartDate()) || selectedDate.isAfter(parentTrip.getEndDate())) {
                    showError("Activity date must fall between trip dates: " 
                            + parentTrip.getStartDate() + " and " + parentTrip.getEndDate());
                    return;
                }
            }

            LocalTime start = parseTimeFlexible(startTimeField.getText().trim());
            LocalTime end = parseTimeFlexible(endTimeField.getText().trim());

            if (start == null || end == null) {
                showError("Invalid time format! Enter time as HH:mm (e.g., 09:30 or 14:00).");
                return;
            }

            // Measurement 2: End time must strictly follow start time
            if (!end.isAfter(start)) {
                showError("End time (" + end + ") must be after start time (" + start + ").");
                return;
            }

            // 3. Conflict detection against other activities in the parent trip
            if (parentTrip != null) {
                Activity conflict = parentTrip.findConflictingActivity(selectedDate, start, end, activity.getId());
                if (conflict != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Schedule Conflict Warning");
                    alert.setHeaderText("Time Slot Already Booked!");
                    alert.setContentText(
                        "This activity overlaps with: \n" +
                        "• \"" + conflict.getTitle() + "\" (" + conflict.getStartTime() + " - " + conflict.getEndTime() + ")\n\n" +
                        "Do you still want to save this overlapping schedule?"
                    );

                    ButtonType proceedBtn = new ButtonType("Save Anyway", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancelBtnType = new ButtonType("Adjust Time", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(proceedBtn, cancelBtnType);

                    var result = alert.showAndWait();
                    if (result.isEmpty() || result.get() != proceedBtn) {
                        return; // User opted to fix the time slot
                    }
                }
            }

            // Save details if valid or acknowledged
            activity.setTitle(titleField.getText().trim());
            activity.setCategory(catBox.getValue());
            activity.setPriority(prioBox.getValue());
            activity.setStatus(statusBox.getValue());
            activity.setDate(selectedDate);
            activity.setStartTime(start);
            activity.setEndTime(end);
            activity.setDescription(descArea.getText().trim());

            TripStorage.saveTrips();
            SceneManager.showActivitiesScreen();
        });

        Button cancelBtn = new Button("Back to Activities");
        cancelBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 18; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> SceneManager.showActivitiesScreen());

        HBox btnRow = new HBox(12, saveBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(titleLbl, idBadge, grid, btnRow);

        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(30));

        ScrollPane sp = new ScrollPane(wrapper);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        this.setCenter(sp);
    }

    private LocalTime parseTimeFlexible(String input) {
        if (input == null || input.isBlank()) return null;
        String cleaned = input.trim();
        if (cleaned.length() == 4 && cleaned.charAt(1) == ':') {
            cleaned = "0" + cleaned;
        }
        if (cleaned.length() > 5) {
            cleaned = cleaned.substring(0, 5);
        }
        try {
            return LocalTime.parse(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}