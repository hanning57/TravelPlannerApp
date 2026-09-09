package com.travelplanner;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import com.travelplanner.model.Activity;
import com.travelplanner.model.User;
import com.travelplanner.storage.TripStorage;
import com.travelplanner.ui.*;

public class SceneManager {

    private static StackPane rootContentArea;
    private static Node sidebarNode;
    private static App appInstance;

    public static void init(StackPane contentArea, Node sidebar, App app) {
        rootContentArea = contentArea;
        sidebarNode = sidebar;
        appInstance = app;
    }

    public static void setScreen(Node screenNode, boolean showSidebar) {
        if (rootContentArea != null) {
            rootContentArea.getChildren().setAll(screenNode);
        }
        if (sidebarNode != null) {
            sidebarNode.setVisible(showSidebar);
            sidebarNode.setManaged(showSidebar);
        }
    }

    public static void showLoginScreen() {
        setScreen(new LoginScreen(), false);
    }

    public static void showSignUpScreen() {
        setScreen(new SignUpScreen(), false);
    }

    public static void showDashboardScreen() {
        if (appInstance != null) {
            appInstance.updateUserProfileBadge();
            appInstance.setActiveNav("Dashboard");
        }
        TripStorage.loadUserTrips();
        Dashboard dashboard = new Dashboard(TripStorage.trips);
        setScreen(dashboard.getView(), true);
    }

    public static void showActivitiesScreen() {
        if (appInstance != null) {
            appInstance.setActiveNav("Activities Management");
        }
        TripStorage.loadUserTrips();
        ActivityManager activities = new ActivityManager(TripStorage.trips, SceneManager::showActivitiesScreen);
        setScreen(activities.getView(), true);
    }

    public static void showSearchScreen() {
        if (appInstance != null) {
            appInstance.setActiveNav("Search");
        }
        SearchScreen search = new SearchScreen();
        setScreen(search, true);
    }

    public static void showActivityDetailsScreen(Activity activity) {
        ActivityEditScreen editScreen = new ActivityEditScreen(activity);
        setScreen(editScreen, true);
    }

    public static void showSettingsScreen() {
        if (appInstance != null) {
            appInstance.setActiveNav("Settings");
        }
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            user = new User("guest", "pass", "Guest User", "guest@example.com", "Traveler");
        }
        UserProfileScreen profileView = new UserProfileScreen(user, () -> {
            if (appInstance != null) {
                appInstance.updateUserProfileBadge();
            }
            showSettingsScreen();
        });
        setScreen(profileView.getView(), true);
    }

    public static void logout() {
        SessionManager.clearSession();
        showLoginScreen();
    }

    public static Button buttonTemplate(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: #0B5FFF; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 9 16; " +
            "-fx-cursor: hand;"
        );
        return btn;
    }
}