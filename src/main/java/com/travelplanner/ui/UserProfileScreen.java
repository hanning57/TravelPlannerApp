package com.travelplanner.ui;

import com.travelplanner.model.User;
import com.travelplanner.storage.UserStorage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UserProfileScreen {

    private final User user;
    private final Runnable onProfileUpdated;

    public UserProfileScreen(User user, Runnable onProfileUpdated) {
        this.user = user;
        this.onProfileUpdated = onProfileUpdated;
    }

    public Node getView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));
        root.setMaxWidth(800);

        // Header Title
        VBox headerBox = new VBox(4);
        Label title = new Label("User Profile & Account");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");
        Label subtitle = new Label("Manage your account details and profile information");
        subtitle.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
        headerBox.getChildren().addAll(title, subtitle);

        // Profile Badge Card
        HBox profileBadgeCard = createProfileHeaderCard();

        // Details Form Card
        VBox formCard = createFormCard();

        root.getChildren().addAll(headerBox, profileBadgeCard, formCard);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8F9FA;");
        return scrollPane;
    }

    private HBox createProfileHeaderCard() {
        HBox card = new HBox(20);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #EAEAEA; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 8, 0, 0, 3);"
        );

        Label avatar = new Label(user.getInitials());
        avatar.setStyle(
            "-fx-background-color: #6366F1; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 20px; " +
            "-fx-background-radius: 50; " +
            "-fx-alignment: center;"
        );
        avatar.setPrefSize(64, 64);

        VBox userDetails = new VBox(4);
        Label nameLbl = new Label(user.getFullName());
        nameLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Label usernameLbl = new Label("@" + user.getUsername() + "  •  " + user.getRole());
        usernameLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        userDetails.getChildren().addAll(nameLbl, usernameLbl);

        card.getChildren().addAll(avatar, userDetails);
        return card;
    }

    private VBox createFormCard() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #EAEAEA; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 8, 0, 0, 3);"
        );

        Label secTitle = new Label("Edit Details");
        secTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);

        TextField fullNameField = new TextField(user.getFullName());
        TextField emailField = new TextField(user.getEmail());
        
        TextField usernameField = new TextField(user.getUsername());
        usernameField.setDisable(true); // Read-only

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Traveler", "Trip Admin", "Guide");
        roleBox.setValue(user.getRole());

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Username:"), 0, 2);
        grid.add(usernameField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleBox, 1, 3);

        Separator sep = new Separator();

        // Password Change Fields
        Label pwdTitle = new Label("Security & Password");
        pwdTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        GridPane pwdGrid = new GridPane();
        pwdGrid.setHgap(16);
        pwdGrid.setVgap(12);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Leave blank to keep current");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");

        pwdGrid.add(new Label("New Password:"), 0, 0);
        pwdGrid.add(newPasswordField, 1, 0);
        pwdGrid.add(new Label("Confirm Password:"), 0, 1);
        pwdGrid.add(confirmPasswordField, 1, 1);

        // Buttons
        Button saveBtn = new Button("Save Profile Changes");
        saveBtn.setStyle(
            "-fx-background-color: #0B5FFF; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 20; " +
            "-fx-cursor: hand;"
        );
        saveBtn.setOnAction(e -> {
            String newFullName = fullNameField.getText().trim();
            if (newFullName.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Full name cannot be empty.");
                return;
            }

            String newPwd = newPasswordField.getText().trim();
            String confirmPwd = confirmPasswordField.getText().trim();

            if (!newPwd.isEmpty()) {
                if (newPwd.length() < 4) {
                    showAlert(Alert.AlertType.ERROR, "New password must be at least 4 characters long.");
                    return;
                }
                if (!newPwd.equals(confirmPwd)) {
                    showAlert(Alert.AlertType.ERROR, "New passwords do not match!");
                    return;
                }
            }

            // Update in-memory metadata
            user.setFullName(newFullName);
            user.setEmail(emailField.getText().trim());
            user.setRole(roleBox.getValue());

            // Persist to database
            boolean success = UserStorage.updateUser(user, newPwd.isEmpty() ? null : newPwd);

            if (success) {
                // Ensure SessionManager holds the newly updated User object
                com.travelplanner.SessionManager.setCurrentUser(user);
                
                showAlert(Alert.AlertType.INFORMATION, "Profile updated successfully! If you changed your password, use the new password on next login.");
                newPasswordField.clear();
                confirmPasswordField.clear();

                if (onProfileUpdated != null) {
                    onProfileUpdated.run();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to update profile in database. Please try again.");
            }
        });

        card.getChildren().addAll(secTitle, grid, sep, pwdTitle, pwdGrid, saveBtn);
        return card;
    }

    private void showAlert(Alert.AlertType type, String text) {
        Alert alert = new Alert(type, text);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}