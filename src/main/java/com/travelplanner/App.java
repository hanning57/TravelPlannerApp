package com.travelplanner;

import com.travelplanner.model.User;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    private final List<Button> navButtons = new ArrayList<>();
    private Label avatarLabel;
    private Label userNameLabel;
    private Label userRoleLabel;

    @Override
    public void start(Stage primaryStage) {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: #F8F9FA;");

        StackPane contentArea = new StackPane();
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        VBox sidebar = createSidebar();
        root.getChildren().addAll(sidebar, contentArea);

        SceneManager.init(contentArea, sidebar, this);

        // Check for an existing saved session
        User sessionUser = SessionManager.loadSession();
        if (sessionUser != null) {
            SessionManager.setCurrentUser(sessionUser);
            SceneManager.showDashboardScreen();
        } else {
            SceneManager.showLoginScreen();
        }

        Scene scene = new Scene(root, 1280, 750);
        primaryStage.setTitle("Travel Planner Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #EAEAEA; -fx-border-width: 0 1px 0 0;");

        Label brandLabel = new Label("Travel Planner");
        brandLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 10; -fx-text-fill: #1A1A1A;");

        VBox navItems = new VBox(8);
        String[] menus = {"Dashboard", "Activities Management", "Search", "Settings"};

        for (String menu : menus) {
            Button item = new Button(menu);
            item.setMaxWidth(Double.MAX_VALUE);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(10, 14, 10, 14));
            item.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 13px; -fx-cursor: hand;");

            item.setOnAction(e -> {
                setActiveNav(menu);
                switch (menu) {
                    case "Dashboard" -> SceneManager.showDashboardScreen();
                    case "Activities Management" -> SceneManager.showActivitiesScreen();
                    case "Search" -> SceneManager.showSearchScreen();
                    case "Settings" -> SceneManager.showSettingsScreen();
                }
            });

            navButtons.add(item);
            navItems.getChildren().add(item);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Dynamic Profile Widget at the bottom of the sidebar
        HBox profileBox = new HBox(10);
        profileBox.setAlignment(Pos.CENTER_LEFT);

        avatarLabel = new Label("JD");
        avatarLabel.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50; -fx-alignment: center;");
        avatarLabel.setPrefSize(38, 38);

        VBox nameDetails = new VBox(2);
        userNameLabel = new Label("John Doe");
        userNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        userRoleLabel = new Label("Traveler");
        userRoleLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        nameDetails.getChildren().addAll(userNameLabel, userRoleLabel);

        Button logoutBtn = new Button("⏻");
        logoutBtn.setTooltip(new javafx.scene.control.Tooltip("Logout"));
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> SceneManager.logout());

        Region profileSpacer = new Region();
        HBox.setHgrow(profileSpacer, Priority.ALWAYS);

        profileBox.getChildren().addAll(avatarLabel, nameDetails, profileSpacer, logoutBtn);

        sidebar.getChildren().addAll(brandLabel, navItems, spacer, profileBox);
        return sidebar;
    }

    public void updateUserProfileBadge() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            avatarLabel.setText(user.getInitials());
            userNameLabel.setText(user.getFullName());
            userRoleLabel.setText(user.getRole());
        }
    }

    public void setActiveNav(String activeTitle) {
        for (Button btn : navButtons) {
            if (btn.getText().equalsIgnoreCase(activeTitle)) {
                btn.setStyle("-fx-background-color: #EBF3FF; -fx-text-fill: #0B5FFF; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 14; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-weight: normal; -fx-padding: 10 14; -fx-cursor: hand;");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}