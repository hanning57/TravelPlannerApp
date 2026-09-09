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

public class LoginScreen extends StackPane {

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label messageLabel = new Label();

    public LoginScreen() {
        this.setStyle("-fx-background-color: #F8F9FA;");

        // Centered Auth Card Container
        VBox card = new VBox(20);
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

        // Header Section
        Label appBadge = new Label("Travel Planner");
        appBadge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0B5FFF;");

        Label title = new Label("Welcome Back");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Label subtitle = new Label("Please sign in to continue planning your trips");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        VBox headerBox = new VBox(4, appBadge, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Input Fields
        String fieldStyle = 
            "-fx-background-color: #F9FAFB; " +
            "-fx-border-color: #E5E7EB; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 14; " +
            "-fx-font-size: 13px;";

        usernameField.setPromptText("Enter your username");
        usernameField.setStyle(fieldStyle);

        passwordField.setPromptText("Enter your password");
        passwordField.setStyle(fieldStyle);

        VBox userBox = new VBox(6, createFieldLabel("Username"), usernameField);
        VBox passBox = new VBox(6, createFieldLabel("Password"), passwordField);

        // Buttons
        Button btnLogin = new Button("Sign In");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle(
            "-fx-background-color: #0B5FFF; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 11 16; " +
            "-fx-cursor: hand;"
        );
        btnLogin.setOnAction(e -> handleLogin());

        // Trigger login when pressing Enter
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> handleLogin());

        HBox signUpFooter = new HBox(6);
        signUpFooter.setAlignment(Pos.CENTER);
        Label noAccLabel = new Label("Don't have an account?");
        noAccLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        Button btnGoToSignUp = new Button("Create one");
        btnGoToSignUp.setStyle("-fx-background-color: transparent; -fx-text-fill: #0B5FFF; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;");
        btnGoToSignUp.setOnAction(e -> SceneManager.showSignUpScreen());

        signUpFooter.getChildren().addAll(noAccLabel, btnGoToSignUp);

        messageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");
        messageLabel.setWrapText(true);

        card.getChildren().addAll(headerBox, userBox, passBox, btnLogin, signUpFooter, messageLabel);
        this.getChildren().add(card);
    }

    private Label createFieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        return lbl;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        User user = UserStorage.authenticate(username, password);
        if (user == null) {
            showError("Invalid username or password.");
            return;
        }

        SessionManager.setCurrentUser(user);
        SessionManager.saveSession(user.getUsername());

        messageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #059669;");
        messageLabel.setText("Login successful! Redirecting...");

        SceneManager.showDashboardScreen();
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");
        messageLabel.setText(msg);
    }
}
