package com.travelplanner.ui;

import com.travelplanner.SceneManager;
import com.travelplanner.model.Activity;
import com.travelplanner.model.Trip;
import com.travelplanner.storage.TripStorage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ActivityManager {

    private final List<Trip> trips;
    private final Runnable refreshCallback;

    public ActivityManager(List<Trip> trips, Runnable refreshCallback) {
        this.trips = trips;
        this.refreshCallback = refreshCallback;
    }

    public Node getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(30));

        Label title = new Label("Activities Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Button newActivityBtn = new Button("+ Add Activity to Trip");
        newActivityBtn.setStyle("-fx-background-color: #0B5FFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        newActivityBtn.setOnAction(e -> openAddActivityDialog());

        VBox activitiesList = new VBox(14);

        if (trips == null || trips.isEmpty()) {
            Label noTripsLbl = new Label("No trips found. Create a trip first.");
            noTripsLbl.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
            activitiesList.getChildren().add(noTripsLbl);
        } else {
            for (Trip trip : trips) {
                String safeTripId = (trip.getId() != null && trip.getId().length() >= 8)
                        ? trip.getId().substring(0, 8)
                        : String.valueOf(trip.getId());

                Label tripHeading = new Label("Trip ID: " + safeTripId + "  |  " + trip.getName() + " (" + trip.getDestination() + ")");
                tripHeading.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1E40AF; -fx-padding: 10 0 2 0;");
                activitiesList.getChildren().add(tripHeading);

                if (trip.getActivities() == null || trip.getActivities().isEmpty()) {
                    Label noAct = new Label("    No activities assigned.");
                    noAct.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                    activitiesList.getChildren().add(noAct);
                    continue;
                }

                for (Activity act : trip.getActivities()) {
                    HBox row = new HBox(12);
                    row.setStyle("-fx-background-color: white; -fx-padding: 12 16; -fx-background-radius: 8; -fx-border-color: #ECECEC; -fx-border-radius: 8;");
                    row.setAlignment(Pos.CENTER_LEFT);

                    String safeActId = (act.getId() != null && act.getId().length() >= 6)
                            ? act.getId().substring(0, 6)
                            : String.valueOf(act.getId());

                    Label idTag = new Label("ID: " + safeActId);
                    idTag.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 3 6; -fx-background-radius: 4; -fx-font-family: monospace; -fx-font-size: 11px;");

                    VBox info = new VBox(3);
                    Label actName = new Label(act.getTitle() + "  [" + (act.getCategory() != null ? act.getCategory() : "General") + "]");
                    actName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                    String startTimeStr = act.getStartTime() != null ? act.getStartTime().toString() : "--:--";
                    String endTimeStr = act.getEndTime() != null ? act.getEndTime().toString() : "--:--";
                    String dateStr = act.getDate() != null ? act.getDate().toString() : "No Date";

                    Label actMeta = new Label(dateStr + " (" + startTimeStr + " - " + endTimeStr + ") | Status: " + act.getStatus() + " | Priority: " + act.getPriority());
                    actMeta.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

                    String descText = (act.getDescription() == null || act.getDescription().isBlank()) ? "No description" : act.getDescription();
                    Label actDesc = new Label(descText);
                    actDesc.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-style: italic;");

                    info.getChildren().addAll(actName, actMeta, actDesc);

                    Region rowSpacer = new Region();
                    HBox.setHgrow(rowSpacer, Priority.ALWAYS);

                    Button editBtn = new Button("Edit");
                    editBtn.setStyle("-fx-background-color: #EBF3FF; -fx-text-fill: #0B5FFF; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                    editBtn.setOnAction(e -> SceneManager.showActivityDetailsScreen(act));

                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                    deleteBtn.setOnAction(e -> {
                        trip.getActivities().removeIf(a -> a.getId() != null && a.getId().equals(act.getId()));
                        TripStorage.saveTrips();
                        if (refreshCallback != null) {
                            refreshCallback.run();
                        }
                    });

                    HBox actions = new HBox(8, editBtn, deleteBtn);
                    actions.setAlignment(Pos.CENTER_RIGHT);

                    // Elements added exactly once
                    row.getChildren().addAll(idTag, info, rowSpacer, actions);
                    activitiesList.getChildren().add(row);
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(activitiesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");

        view.getChildren().addAll(title, newActivityBtn, scrollPane);
        return view;
    }

    private void openAddActivityDialog() {
        if (trips == null || trips.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Create a trip before adding activities.");
            alert.showAndWait();
            return;
        }

        Dialog<Activity> dialog = new Dialog<>();
        dialog.setTitle("Add New Activity");

        ButtonType saveBtnType = new ButtonType("Add Activity", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<Trip> tripDropdown = new ComboBox<>();
        tripDropdown.getItems().addAll(trips);
        tripDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Trip item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    String sId = (item.getId() != null && item.getId().length() >= 8) ? item.getId().substring(0, 8) : item.getId();
                    setText("[" + sId + "] " + item.getName());
                }
            }
        });
        tripDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Trip item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    String sId = (item.getId() != null && item.getId().length() >= 8) ? item.getId().substring(0, 8) : item.getId();
                    setText("[" + sId + "] " + item.getName());
                }
            }
        });
        tripDropdown.getSelectionModel().selectFirst();

        TextField titleField = new TextField();
        TextField descField = new TextField();

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Sightseeing", "Food", "Leisure", "Transport", "Museum", "Sports");
        categoryBox.getSelectionModel().selectFirst();

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.getSelectionModel().select("Medium");

        ComboBox<String> recurrenceBox = new ComboBox<>();
        recurrenceBox.getItems().addAll("None", "Daily", "Weekly");
        recurrenceBox.getSelectionModel().selectFirst();

        DatePicker datePicker = new DatePicker(trips.get(0).getStartDate());
        TextField startTimeField = new TextField("09:00");
        TextField endTimeField = new TextField("11:00");

        grid.add(new Label("Target Trip:"), 0, 0);
        grid.add(tripDropdown, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);
        grid.add(new Label("Priority:"), 0, 4);
        grid.add(priorityBox, 1, 4);
        grid.add(new Label("Recurrence:"), 0, 5);
        grid.add(recurrenceBox, 1, 5);
        grid.add(new Label("Date:"), 0, 6);
        grid.add(datePicker, 1, 6);
        grid.add(new Label("Start Time:"), 0, 7);
        grid.add(startTimeField, 1, 7);
        grid.add(new Label("End Time:"), 0, 8);
        grid.add(endTimeField, 1, 8);

        dialog.getDialogPane().setContent(grid);

        // Update DatePicker constraints whenever the selected trip changes
        Runnable updateDateConstraints = () -> {
            Trip selectedTrip = tripDropdown.getValue();
            if (selectedTrip != null && selectedTrip.getStartDate() != null && selectedTrip.getEndDate() != null) {
                datePicker.setValue(selectedTrip.getStartDate());
                datePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) return;
                        if (item.isBefore(selectedTrip.getStartDate()) || item.isAfter(selectedTrip.getEndDate())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #9CA3AF;");
                        }
                    }
                });
            }
        };

        tripDropdown.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateDateConstraints.run());
        updateDateConstraints.run();

        dialog.setResultConverter(button -> {
            if (button == saveBtnType) {
                Trip targetTrip = tripDropdown.getValue();
                String title = titleField.getText().trim();
                LocalDate chosenDate = datePicker.getValue();

                if (title.isBlank()) {
                    showInlineAlert("Validation Error", "Activity title cannot be empty.");
                    return null;
                }

                // Measurement check: activity within trip dates
                if (targetTrip != null && (chosenDate.isBefore(targetTrip.getStartDate()) || chosenDate.isAfter(targetTrip.getEndDate()))) {
                    showInlineAlert("Date Error", "Activity must occur during the trip: " 
                            + targetTrip.getStartDate() + " to " + targetTrip.getEndDate());
                    return null;
                }

                LocalTime start = safeParseTime(startTimeField.getText().trim(), null);
                LocalTime end = safeParseTime(endTimeField.getText().trim(), null);

                if (start == null || end == null) {
                    showInlineAlert("Time Error", "Please provide times in HH:mm format (e.g. 09:30).");
                    return null;
                }

                // Measurement check: end after start
                // End after start check
                if (!end.isAfter(start)) {
                    showInlineAlert("Time Error", "End time must be strictly after start time.");
                    return null;
                }

                // Check for time collision on the destination trip
                if (targetTrip != null) {
                    Activity conflict = targetTrip.findConflictingActivity(chosenDate, start, end, null);
                    if (conflict != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Schedule Conflict Warning");
                        alert.setHeaderText("Time Slot Collision");
                        alert.setContentText(
                            "This slot overlaps with an existing activity:\n" +
                            "• \"" + conflict.getTitle() + "\" (" + conflict.getStartTime() + " - " + conflict.getEndTime() + ")\n\n" +
                            "Would you like to add it anyway?"
                        );

                        ButtonType proceed = new ButtonType("Add Anyway", ButtonBar.ButtonData.OK_DONE);
                        ButtonType fix = new ButtonType("Adjust Time", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(proceed, fix);

                        var choice = alert.showAndWait();
                        if (choice.isEmpty() || choice.get() != proceed) {
                            return null;
                        }
                    }
                }

                Activity act = new Activity(
                        title,
                        chosenDate,
                        start,
                        end,
                        categoryBox.getValue(),
                        priorityBox.getValue(),
                        recurrenceBox.getValue(),
                        descField.getText().trim()
                );
                targetTrip.addActivity(act);
                TripStorage.saveTrips();
                return act;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(act -> {
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        });
    }

    private LocalTime safeParseTime(String text, LocalTime fallback) {
        try {
            if (text == null || text.isBlank()) return fallback;
            if (text.length() == 4 && text.charAt(1) == ':') text = "0" + text;
            if (text.length() > 5) text = text.substring(0, 5);
            return LocalTime.parse(text);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void showInlineAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}