package com.travelplanner.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PlaceHolder {

    private final String screenName;

    public PlaceHolder(String screenName) {
        this.screenName = screenName;
    }

    public Node getView() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);

        Label title = new Label(screenName);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4B5563;");

        Label subtitle = new Label("Feature coming soon");
        subtitle.setStyle("-fx-text-fill: #9CA3AF;");

        box.getChildren().addAll(title, subtitle);
        return box;
    }
}
