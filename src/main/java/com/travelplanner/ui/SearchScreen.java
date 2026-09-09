package com.travelplanner.ui;

import java.time.LocalTime;
import java.util.List;

import com.travelplanner.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.travelplanner.model.Activity;
import com.travelplanner.model.Landmark;
import com.travelplanner.model.Trip;
import com.travelplanner.storage.TripStorage;


public class SearchScreen extends BorderPane {

    private final ObservableList<Activity> allActivitiesList = FXCollections.observableArrayList();
    private final FilteredList<Activity> filteredActivities = new FilteredList<>(allActivitiesList, p -> true);
    private final SortedList<Activity> sortedActivities = new SortedList<>(filteredActivities);

    private TextField tfActivitySearch;
    private ListView<Activity> lvActivities;
    private ListView<String> lvLandmarks;
    private ComboBox<Trip> cbTripSelect;

    public SearchScreen() {
        this.setStyle("-fx-background-color: #F8F9FA;");
        loadActivitiesFromStorage();

        HBox mainContent = new HBox(24, buildActivitySearchPane(), buildLandmarkPane());
        mainContent.setPadding(new Insets(24));
        mainContent.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA; -fx-border-color: transparent;");

        this.setCenter(scrollPane);
    }

    private VBox buildActivitySearchPane() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #EAEAEA; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);"
        );
        HBox.setHgrow(card, Priority.ALWAYS);

        // Header
        VBox headerBox = new VBox(4);
        Label lblActivityTitle = new Label("Search Activities");
        lblActivityTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");
        Label lblSubtitle = new Label("Find and filter activities across all your planned trips");
        lblSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        headerBox.getChildren().addAll(lblActivityTitle, lblSubtitle);

        // Search Input Bar
        tfActivitySearch = new TextField();
        tfActivitySearch.setPromptText("Search by title, description, trip, category...");
        tfActivitySearch.setStyle(
            "-fx-background-color: #F8F9FA; " +
            "-fx-border-color: #E2E8F0; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 14; " +
            "-fx-font-size: 13px;"
        );
        HBox.setHgrow(tfActivitySearch, Priority.ALWAYS);

        // Sort Toggles
        ToggleGroup sortGroup = new ToggleGroup();
        ToggleButton btnAsc = new ToggleButton("A → Z");
        ToggleButton btnDesc = new ToggleButton("Z → A");
        btnAsc.setToggleGroup(sortGroup);
        btnDesc.setToggleGroup(sortGroup);
        btnAsc.setSelected(true);

        String activeToggleStyle = "-fx-background-color: #EBF3FF; -fx-text-fill: #0B5FFF; -fx-font-weight: bold; -fx-border-color: #93C5FD; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;";
        String inactiveToggleStyle = "-fx-background-color: white; -fx-text-fill: #64748B; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-cursor: hand;";

        btnAsc.setStyle(activeToggleStyle);
        btnDesc.setStyle(inactiveToggleStyle);

        sortGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == btnAsc) {
                btnAsc.setStyle(activeToggleStyle);
                btnDesc.setStyle(inactiveToggleStyle);
                sortedActivities.setComparator((a1, a2) -> a1.getTitle().compareToIgnoreCase(a2.getTitle()));
            } else if (newVal == btnDesc) {
                btnDesc.setStyle(activeToggleStyle);
                btnAsc.setStyle(inactiveToggleStyle);
                sortedActivities.setComparator((a1, a2) -> a2.getTitle().compareToIgnoreCase(a1.getTitle()));
            }
        });

        sortedActivities.setComparator((a1, a2) -> a1.getTitle().compareToIgnoreCase(a2.getTitle()));

        HBox sortControls = new HBox(8, btnAsc, btnDesc);
        sortControls.setAlignment(Pos.CENTER_RIGHT);

        HBox searchRow = new HBox(12, tfActivitySearch, sortControls);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        // Activities List with Dashboard Card-Like Cell Factory
        lvActivities = new ListView<>(sortedActivities);
        lvActivities.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        VBox.setVgrow(lvActivities, Priority.ALWAYS);

        lvActivities.setCellFactory(param -> new ListCell<Activity>() {
            @Override
            protected void updateItem(Activity act, boolean empty) {
                super.updateItem(act, empty);

                if (empty || act == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox row = new HBox(12);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(10, 14, 10, 14));
                    row.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #EEF2F6; " +
                        "-fx-border-radius: 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 6, 0, 0, 2);"
                    );

                    Region accentBar = new Region();
                    accentBar.setPrefWidth(4);
                    accentBar.setPrefHeight(38);
                    accentBar.setStyle("-fx-background-color: #0B5FFF; -fx-background-radius: 2;");

                    VBox textDetails = new VBox(3);
                    HBox titleLine = new HBox(8);
                    titleLine.setAlignment(Pos.CENTER_LEFT);

                    Label titleLbl = new Label(act.getTitle());
                    titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E293B;");

                    String tripName = act.getTrip() != null ? act.getTrip().getName() : "Unassigned";
                    Label tripTag = new Label("• " + tripName);
                    tripTag.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

                    titleLine.getChildren().addAll(titleLbl, tripTag);

                    Label metaLbl = new Label(act.getDate() + " (" + act.getStartTime() + " - " + act.getEndTime() + ") • " + act.getCategory() + " • " + act.getStatus());
                    metaLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

                    textDetails.getChildren().addAll(titleLine, metaLbl);
                    row.getChildren().addAll(accentBar, textDetails);

                    setGraphic(row);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
                }
            }
        });

        tfActivitySearch.textProperty().addListener((obs, oldVal, newVal) -> applyActivityFilter(newVal));

        card.getChildren().addAll(headerBox, searchRow, lvActivities);
        return card;
    }

    private VBox buildLandmarkPane() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setPrefWidth(460);
        card.setMaxWidth(520);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #EAEAEA; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);"
        );

        // Header
        VBox headerBox = new VBox(4);
        Label lblLandmarkTitle = new Label("Landmark Discovery");
        lblLandmarkTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");
        Label lblLandmarkSub = new Label("Explore landmarks based on destination");
        lblLandmarkSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
        headerBox.getChildren().addAll(lblLandmarkTitle, lblLandmarkSub);

        // Trip Selector Dropdown
        cbTripSelect = new ComboBox<>();
        cbTripSelect.setItems(FXCollections.observableArrayList(TripStorage.trips));
        cbTripSelect.setPromptText("Select a trip destination...");
        cbTripSelect.setMaxWidth(Double.MAX_VALUE);
        cbTripSelect.setStyle(
            "-fx-background-color: #F8F9FA; " +
            "-fx-border-color: #E2E8F0; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 4 8;"
        );

        cbTripSelect.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Trip trip, boolean empty) {
                super.updateItem(trip, empty);
                setText(empty || trip == null ? "" : trip.getName() + " (" + trip.getDestination() + ")");
            }
        });
        cbTripSelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Trip trip, boolean empty) {
                super.updateItem(trip, empty);
                setText(empty || trip == null ? "" : trip.getName() + " (" + trip.getDestination() + ")");
            }
        });

        cbTripSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldTrip, newTrip) -> {
            if (newTrip != null) {
                autoSearchLandmarks(newTrip.getDestination());
            }
        });

        // Landmark Result Section
        Label lblResultHeader = new Label("Suggested Landmarks");
        lblResultHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #334155;");

        lvLandmarks = new ListView<>();
        lvLandmarks.setPrefHeight(260);
        lvLandmarks.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        VBox.setVgrow(lvLandmarks, Priority.ALWAYS);

        lvLandmarks.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setPadding(new Insets(10, 12, 10, 12));
                    box.setStyle(
                        "-fx-background-color: #FAFAFA; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-color: #EEF2F6; " +
                        "-fx-border-radius: 6;"
                    );

                    Label icon = new Label("📍");
                    Label title = new Label(item);
                    title.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155; -fx-font-weight: 500;");

                    box.getChildren().addAll(icon, title);
                    setGraphic(box);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 3 0;");
                }
            }
        });

        // Action Buttons
        Button btnAddToTrip = new Button("+ Add Selected Landmark to Trip");
        btnAddToTrip.setMaxWidth(Double.MAX_VALUE);
        btnAddToTrip.setStyle("-fx-background-color: #0B5FFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 16; -fx-cursor: hand;");
        btnAddToTrip.setOnAction(e -> addLandmarkToTrip());

        Button btnGoogleSearch = new Button("Search on Google Maps");
        btnGoogleSearch.setMaxWidth(Double.MAX_VALUE);
        btnGoogleSearch.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 16; -fx-cursor: hand;");
        btnGoogleSearch.setOnAction(e -> {
            Trip selectedTrip = cbTripSelect.getValue();
            if (selectedTrip != null) {
                String query = selectedTrip.getDestination().replace(" ", "+");
                String url = "https://www.google.com/maps/search/" + query + "+landmarks";
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                } catch (Exception ex) {
                    showAlert("Error", "Could not open browser.");
                }
            } else {
                showAlert("Selection Required", "Please select a trip first.");
            }
        });

        Button btnBack = new Button("Back to Dashboard");
        btnBack.setMaxWidth(Double.MAX_VALUE);
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748B; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnBack.setOnAction(e -> SceneManager.showDashboardScreen());

        card.getChildren().addAll(headerBox, cbTripSelect, lblResultHeader, lvLandmarks, btnAddToTrip, btnGoogleSearch, btnBack);
        return card;
    }

    private void applyActivityFilter(String input) {
        if (input == null || input.trim().isEmpty()) {
            filteredActivities.setPredicate(p -> true);
            return;
        }

        String lowerInput = input.toLowerCase();

        filteredActivities.setPredicate(activity -> {
            String tripName = activity.getTrip() != null ? activity.getTrip().getName().toLowerCase() : "";
            String desc = activity.getDescription() != null ? activity.getDescription().toLowerCase() : "";
            String cat = activity.getCategory() != null ? activity.getCategory().toLowerCase() : "";
            String priority = activity.getPriority() != null ? activity.getPriority().toLowerCase() : "";
            String status = activity.getStatus() != null ? activity.getStatus().toLowerCase() : "";
            String time = activity.getStartTime() != null ? activity.getStartTime().toString() : "";

            return activity.getTitle().toLowerCase().contains(lowerInput)
                    || desc.contains(lowerInput)
                    || tripName.contains(lowerInput)
                    || cat.contains(lowerInput)
                    || priority.contains(lowerInput)
                    || status.contains(lowerInput)
                    || time.contains(lowerInput);
        });
    }

    private void autoSearchLandmarks(String destination) {
        lvLandmarks.getItems().clear();

        if (destination == null || destination.trim().isEmpty()) {
            return;
        }

        String city = destination.split(",")[0].trim();
        List<String> suggestions = Landmark.getSuggestions(city);

        if (suggestions == null || suggestions.isEmpty()) {
            lvLandmarks.getItems().add("No suggested landmarks found for " + city);
        } else {
            lvLandmarks.getItems().addAll(suggestions);
        }
    }

    private void addLandmarkToTrip() {
        String selectedLandmark = lvLandmarks.getSelectionModel().getSelectedItem();
        Trip selectedTrip = cbTripSelect.getValue();

        if (selectedLandmark == null || selectedLandmark.startsWith("No suggested landmarks")) {
            showAlert("Selection Error", "Please select a valid landmark from the list.");
            return;
        }

        if (selectedTrip == null) {
            showAlert("Trip Error", "Please select a trip to add this landmark to.");
            return;
        }

        Activity landmarkActivity = new Activity(
            selectedLandmark,
            selectedTrip.getStartDate() != null ? selectedTrip.getStartDate() : java.time.LocalDate.now(),
            LocalTime.of(10, 0),
            LocalTime.of(12, 0),
            "Sightseeing",
            "Medium",
            "None",
            "Discovered via Search & Landmark Discovery"
        );
        landmarkActivity.setStatus("Planned");
        landmarkActivity.setTrip(selectedTrip);

        selectedTrip.getActivities().add(landmarkActivity);
        TripStorage.saveTrips();

        // Direct immediately to edit screen
        showSuccessAndNavigate(selectedTrip);
        SceneManager.showActivityDetailsScreen(landmarkActivity);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAndNavigate(Trip trip) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success!");
        alert.setHeaderText(null);
        alert.setContentText("Added to " + trip.getName() + ". Redirecting to details screen.");
        alert.showAndWait();

        SceneManager.showActivitiesScreen();
    }

    private void loadActivitiesFromStorage() {
        allActivitiesList.clear();
        if (TripStorage.trips != null) {
            for (Trip trip : TripStorage.trips) {
                if (trip.getActivities() != null) {
                    allActivitiesList.addAll(trip.getActivities());
                }
            }
        }
    }
}