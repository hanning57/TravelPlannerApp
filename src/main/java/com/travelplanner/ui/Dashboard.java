package com.travelplanner.ui;

import com.travelplanner.model.Activity;
import com.travelplanner.model.Trip;
import com.travelplanner.storage.TripStorage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.time.LocalDate;
import java.util.List;

public class Dashboard {

    private final List<Trip> trips;
    private FlowPane tripCardsContainer;

    public Dashboard(List<Trip> trips) {
        this.trips = trips;
    }

    // Keep track of the interactive calendar & dynamic activities box
    private CalendarView calendarView;
    private VBox upcomingActivitiesContainer;

    public Node getView() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");

        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(24));

        HBox headerBar = new HBox();
        headerBar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("My Trips");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addTripBtn = new Button("+ Add New Trip");
        addTripBtn.setStyle("-fx-background-color: #0B5FFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        addTripBtn.setOnAction(e -> openAddTripDialog());

        headerBar.getChildren().addAll(titleLabel, spacer, addTripBtn);

        // Top section: Interactive Calendar connected to dynamic Activities panel
        HBox topGrid = new HBox(24);

        upcomingActivitiesContainer = createUpcomingActivitiesWidget(null);

        // When a user clicks a calendar day, filter activities by that date
        calendarView = new CalendarView(clickedDate -> {
            renderUpcomingActivities(clickedDate);
        });

        // Collect dates that have activities and display dot indicators
        refreshCalendarActivityDots();

        topGrid.getChildren().addAll(calendarView, upcomingActivitiesContainer);

        tripCardsContainer = new FlowPane();
        tripCardsContainer.setHgap(24);
        tripCardsContainer.setVgap(24);
        renderTripCards();

        mainContent.getChildren().addAll(headerBar, topGrid, tripCardsContainer);
        scrollPane.setContent(mainContent);
        return scrollPane;
    }

    private void refreshCalendarActivityDots() {
        java.util.Set<LocalDate> dates = new java.util.HashSet<>();
        for (Trip trip : trips) {
            for (Activity act : trip.getActivities()) {
                if (act.getDate() != null) {
                    dates.add(act.getDate());
                }
            }
        }
        calendarView.setActivityDates(dates);
    }

    private VBox createUpcomingActivitiesWidget(LocalDate filterDate) {
        VBox widget = new VBox(12);
        widget.setPadding(new Insets(20));
        widget.setPrefWidth(380);
        widget.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #ECECEC; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 4);");

        populateActivitiesList(widget, filterDate);
        return widget;
    }

    private void renderUpcomingActivities(LocalDate filterDate) {
        upcomingActivitiesContainer.getChildren().clear();
        populateActivitiesList(upcomingActivitiesContainer, filterDate);
    }

    private void populateActivitiesList(VBox container, LocalDate filterDate) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        String titleText = (filterDate == null) ? "Upcoming Activities" : "Activities on " + filterDate;
        Label label = new Label(titleText);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1A1A1A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (filterDate != null) {
            Button clearFilterBtn = new Button("Show All");
            clearFilterBtn.setStyle("-fx-background-color: #F3F4F6; -fx-font-size: 10px; -fx-text-fill: #4B5563; -fx-cursor: hand; -fx-background-radius: 4;");
            clearFilterBtn.setOnAction(e -> renderUpcomingActivities(null));
            header.getChildren().addAll(label, spacer, clearFilterBtn);
        } else {
            header.getChildren().add(label);
        }

        container.getChildren().add(header);

        int count = 0;
        for (Trip trip : trips) {
            for (Activity act : trip.getActivities()) {
                if (filterDate == null || (act.getDate() != null && filterDate.equals(act.getDate()))) {
                    count++;
                    HBox item = new HBox(10);
                    item.setAlignment(Pos.CENTER_LEFT);
                    item.setPadding(new Insets(8, 10, 8, 10));
                    item.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 8; -fx-border-color: #F0F0F0; -fx-border-radius: 8;");

                    // 1. Color Accent Bar based on Priority
                    Region accentBar = new Region();
                    accentBar.setPrefWidth(4);
                    accentBar.setPrefHeight(34);
                    String priorityColor = getPriorityColor(act.getPriority());
                    accentBar.setStyle("-fx-background-color: " + priorityColor + "; -fx-background-radius: 2;");

                    // 2. Activity Info & Priority Badge
                    VBox textDetails = new VBox(3);
                    HBox titleRow = new HBox(6);
                    titleRow.setAlignment(Pos.CENTER_LEFT);

                    Label actName = new Label(act.getTitle());
                    actName.setStyle("-fx-font-weight: bold; -fx-text-fill: #222222; -fx-font-size: 12px;");
                    actName.setMaxWidth(130);

                    // Colored Priority Pill
                    Label priorityBadge = new Label(act.getPriority() != null ? act.getPriority() : "Medium");
                    priorityBadge.setStyle(getPriorityBadgeStyle(act.getPriority()));

                    titleRow.getChildren().addAll(actName, priorityBadge);

                    String timeStr = act.getStartTime() != null ? act.getStartTime().toString() : "--:--";
                    Label actDate = new Label(act.getDate() + " (" + timeStr + ")");
                    actDate.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

                    textDetails.getChildren().addAll(titleRow, actDate);

                    Region itemSpacer = new Region();
                    HBox.setHgrow(itemSpacer, Priority.ALWAYS);

                    // 3. Instant Status ComboBox
                    ComboBox<String> statusDropdown = new ComboBox<>();
                    statusDropdown.getItems().addAll("Planned", "In Progress", "Completed", "Cancelled");
                    statusDropdown.setValue(act.getStatus() != null ? act.getStatus() : "Planned");
                    statusDropdown.setStyle(getStatusStyle(statusDropdown.getValue()));

                    statusDropdown.setOnAction(e -> {
                        String newStatus = statusDropdown.getValue();
                        act.setStatus(newStatus);
                        statusDropdown.setStyle(getStatusStyle(newStatus));
                        TripStorage.saveTrips();
                        renderTripCards(); // Sync bottom cards immediately
                    });

                    item.getChildren().addAll(accentBar, textDetails, itemSpacer, statusDropdown);
                    container.getChildren().add(item);
                }
            }
        }

        if (count == 0) {
            Label emptyLbl = new Label("No activities scheduled.");
            emptyLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic; -fx-padding: 10 0;");
            container.getChildren().add(emptyLbl);
        }
    }

    // Priority color mapping
    private String getPriorityColor(String priority) {
        if (priority == null) return "#F59E0B"; // Default amber
        return switch (priority.toLowerCase()) {
            case "high" -> "#EF4444";   // Red
            case "low" -> "#10B981";    // Green
            default -> "#F59E0B";       // Amber / Medium
        };
    }

    // Priority pill style badge
    private String getPriorityBadgeStyle(String priority) {
        if (priority == null) priority = "medium";
        return switch (priority.toLowerCase()) {
            case "high" -> "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 1 5; -fx-background-radius: 4;";
            case "low" -> "-fx-background-color: #D1FAE5; -fx-text-fill: #059669; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 1 5; -fx-background-radius: 4;";
            default -> "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 1 5; -fx-background-radius: 4;";
        };
    }

    // Status dropdown compact styling
    private String getStatusStyle(String status) {
        if (status == null) status = "Planned";
        return switch (status.toLowerCase()) {
            case "completed" -> "-fx-background-color: #ECFDF5; -fx-border-color: #A7F3D0; -fx-text-fill: #065F46; -fx-font-size: 11px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-pref-width: 105;";
            case "in progress" -> "-fx-background-color: #EFF6FF; -fx-border-color: #BFDBFE; -fx-text-fill: #1E40AF; -fx-font-size: 11px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-pref-width: 105;";
            case "cancelled" -> "-fx-background-color: #FEF2F2; -fx-border-color: #FECACA; -fx-text-fill: #991B1B; -fx-font-size: 11px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-pref-width: 105;";
            default -> "-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand; -fx-pref-width: 105;";
        };
    }

    private void renderTripCards() {
        tripCardsContainer.getChildren().clear();
        for (Trip trip : trips) {
            tripCardsContainer.getChildren().add(createTripCard(trip));
        }
    }

    private VBox createTripCard(Trip trip) {
        VBox card = new VBox(0);
        card.setPrefWidth(380);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        VBox header = new VBox(4);
        header.setPadding(new Insets(16, 20, 16, 20));
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3B82F6")),
                new Stop(1, Color.web("#8B5CF6")));
        header.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(12, 12, 0, 0, false), Insets.EMPTY)));

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        String safeTripId = (trip.getId() != null && trip.getId().length() >= 8)
                ? trip.getId().substring(0, 8)
                : String.valueOf(trip.getId());

        Label idBadge = new Label("ID: " + safeTripId);
        idBadge.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-family: monospace;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Edit Trip Button
        Button editTripBtn = new Button("Edit");
        editTripBtn.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 4; -fx-padding: 2 8; -fx-cursor: hand;");
        editTripBtn.setOnAction(e -> openEditTripDialog(trip));

        // Delete Trip Button
        Button deleteTripBtn = new Button("Delete");
        deleteTripBtn.setStyle("-fx-background-color: rgba(239,68,68,0.7); -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 4; -fx-padding: 2 8; -fx-cursor: hand;");
        deleteTripBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete '" + trip.getName() + "' and all of its activities?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    trips.removeIf(t -> t.getId().equals(trip.getId()));
                    TripStorage.saveTrips();
                    renderTripCards();
                    if (calendarView != null) {
                        refreshCalendarActivityDots();
                    }
                    renderUpcomingActivities(null);
                }
            });
        });

        HBox tripActions = new HBox(6, editTripBtn, deleteTripBtn);
        tripActions.setAlignment(Pos.CENTER_RIGHT);

        topRow.getChildren().addAll(idBadge, spacer, tripActions);

        Label title = new Label(trip.getName());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label loc = new Label(trip.getDestination() + "  •  " + trip.getStartDate() + " to " + trip.getEndDate());
        loc.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 12px;");

        header.getChildren().addAll(topRow, title, loc);

        VBox body = new VBox(14);
        body.setPadding(new Insets(18));

        if (trip.getActivities().isEmpty()) {
            Label emptyLbl = new Label("No activities yet.");
            emptyLbl.setStyle("-fx-text-fill: #9CA3AF; -fx-font-style: italic;");
            body.getChildren().add(emptyLbl);
        } else {
            for (Activity act : trip.getActivities()) {
                VBox actRow = new VBox(3);

                HBox actTop = new HBox(8);
                actTop.setAlignment(Pos.CENTER_LEFT);

                String safeActId = (act.getId() != null && act.getId().length() >= 6)
                        ? act.getId().substring(0, 6)
                        : String.valueOf(act.getId());

                Label actId = new Label("[" + safeActId + "]");
                actId.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-family: monospace;");
                Label actTitle = new Label(act.getTitle());
                actTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");
                actTop.getChildren().addAll(actId, actTitle);

                Label actMeta = new Label("    " + act.getDate() + " (" + act.getStartTime() + " - " + act.getEndTime() + ") • " + act.getStatus());
                actMeta.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

                actRow.getChildren().addAll(actTop, actMeta);
                body.getChildren().add(actRow);
            }
        }

        card.getChildren().addAll(header, body);
        return card;
    }

    private void openEditTripDialog(Trip trip) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Trip");

        ButtonType saveBtnType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(trip.getName());
        TextField destField = new TextField(trip.getDestination());
        DatePicker startDate = new DatePicker(trip.getStartDate());
        DatePicker endDate = new DatePicker(trip.getEndDate());

        grid.add(new Label("Trip Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Destination:"), 0, 1);
        grid.add(destField, 1, 1);
        grid.add(new Label("Start Date:"), 0, 2);
        grid.add(startDate, 1, 2);
        grid.add(new Label("End Date:"), 0, 3);
        grid.add(endDate, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveBtnType) {
                String name = nameField.getText().trim();
                String dest = destField.getText().trim();
                LocalDate start = startDate.getValue();
                LocalDate end = endDate.getValue();

                if (name.isBlank() || dest.isBlank() || start == null || end == null) {
                    showValidationAlert("Trip name, destination, and dates cannot be empty.");
                    return false;
                }

                if (end.isBefore(start)) {
                    showValidationAlert("Trip end date cannot be earlier than its start date.");
                    return false;
                }

                trip.setName(name);
                trip.setDestination(dest);
                trip.setStartDate(start);
                trip.setEndDate(end);
                TripStorage.saveTrips();
                return true;
            }
            return false;
        });

        dialog.showAndWait().ifPresent(updated -> {
            if (updated) {
                renderTripCards();
            }
        });
    }

    private void openAddTripDialog() {
        Dialog<Trip> dialog = new Dialog<>();
        dialog.setTitle("Create New Trip");

        ButtonType saveBtnType = new ButtonType("Save Trip", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Trip Name");
        TextField destField = new TextField();
        destField.setPromptText("Destination");
        DatePicker startDate = new DatePicker(LocalDate.now());
        DatePicker endDate = new DatePicker(LocalDate.now().plusDays(5));

        grid.add(new Label("Trip Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Destination:"), 0, 1);
        grid.add(destField, 1, 1);
        grid.add(new Label("Start Date:"), 0, 2);
        grid.add(startDate, 1, 2);
        grid.add(new Label("End Date:"), 0, 3);
        grid.add(endDate, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveBtnType) {
                String name = nameField.getText().trim();
                String dest = destField.getText().trim();
                LocalDate start = startDate.getValue();
                LocalDate end = endDate.getValue();

                if (name.isBlank() || dest.isBlank() || start == null || end == null) {
                    showValidationAlert("Trip name, destination, and dates cannot be empty.");
                    return null;
                }

                if (end.isBefore(start)) {
                    showValidationAlert("Trip end date cannot be earlier than its start date.");
                    return null;
                }

                return new Trip(name, dest, start, end);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newTrip -> {
            trips.add(newTrip);
            TripStorage.saveTrips();
            renderTripCards();
        });
    }

    private void showValidationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}