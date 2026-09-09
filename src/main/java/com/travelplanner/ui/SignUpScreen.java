package com.travelplanner.ui;

import com.travelplanner.SceneManager;
import com.travelplanner.SessionManager;
import com.travelplanner.model.User;
import com.travelplanner.storage.UserStorage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class SignUpScreen extends StackPane {

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmField = new PasswordField();
    private final Label messageLabel = new Label();

    public SignUpScreen() {
        this.setStyle("-fx-background-color: #F8F9FA;");

        // Centered Auth Card Container
        VBox card = new VBox(18);
        card.setMaxWidth(420);
        card.setPadding(new Insets(36));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-color: #EAEAEA; " +
            "-fx-border-radius: 16; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 14, 0, 0, 5);"
        );

        Label appBadge = new Label("Travel Planner");
        appBadge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0B5FFF;");

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Label subtitle = new Label("Start tracking trips, activities, and discovery spots");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        VBox headerBox = new VBox(4, appBadge, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        String fieldStyle = 
            "-fx-background-color: #F9FAFB; " +
            "-fx-border-color: #E5E7EB; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 9 14; " +
            "-fx-font-size: 13px;";

        usernameField.setPromptText("Choose a username");
        usernameField.setStyle(fieldStyle);

        passwordField.setPromptText("At least 4 characters");
        passwordField.setStyle(fieldStyle);

        confirmField.setPromptText("Re-type password");
        confirmField.setStyle(fieldStyle);

        VBox userBox = new VBox(5, createFieldLabel("Username"), usernameField);
        VBox passBox = new VBox(5, createFieldLabel("Password"), passwordField);
        VBox confBox = new VBox(5, createFieldLabel("Confirm Password"), confirmField);

        Button btnRegister = new Button("Create Account");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setStyle(
            "-fx-background-color: #0B5FFF; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 11 16; " +
            "-fx-cursor: hand;"
        );
        btnRegister.setOnAction(e -> handleRegister());

        HBox loginFooter = new HBox(6);
        loginFooter.setAlignment(Pos.CENTER);
        Label alreadyHaveLabel = new Label("Already registered?");
        alreadyHaveLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Button btnGoToLogin = new Button("Sign In");
        btnGoToLogin.setStyle("-fx-background-color: transparent; -fx-text-fill: #0B5FFF; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
        btnGoToLogin.setOnAction(e -> SceneManager.showLoginScreen());

        loginFooter.getChildren().addAll(alreadyHaveLabel, btnGoToLogin);

        messageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");
        messageLabel.setWrapText(true);

        card.getChildren().addAll(headerBox, userBox, passBox, confBox, btnRegister, loginFooter, messageLabel);
        this.getChildren().add(card);
    }

    private Label createFieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        return lbl;
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters.");
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        if (UserStorage.usernameExists(username)) {
            showError("Username is already taken.");
            return;
        }

        User user = UserStorage.register(username, password);
        if (user == null) {
            showError("Could not create user. Please try another username.");
            return;
        }

        messageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #059669;");
        messageLabel.setText("Account created! Redirecting to dashboard...");

        SessionManager.setCurrentUser(user);
        SessionManager.saveSession(user.getUsername());
        SceneManager.showDashboardScreen();
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");
        messageLabel.setText(msg);
    }
}
