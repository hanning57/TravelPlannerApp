package com.travelplanner.ui;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

public class CalendarView extends VBox {

    private YearMonth currentYearMonth;
    private final GridPane grid = new GridPane();
    private final Label calenderTitle = new Label();
    private final Consumer<LocalDate> dateSelected;

    private Set<LocalDate> activityDates = Collections.emptySet();
    private LocalDate selectedDate = LocalDate.now();

    public CalendarView(Consumer<LocalDate> dateSelected) {
        this.dateSelected = dateSelected;
        this.currentYearMonth = YearMonth.now();

        this.setPadding(new Insets(20));
        this.setPrefWidth(480);
        this.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #ECECEC; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 4);"
        );

        // Header with Month/Year and Modern Arrow Buttons
        HBox header = createHeader();

        // Grid Configuration
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        populateCalendar(currentYearMonth);

        this.setSpacing(16);
        this.getChildren().addAll(header, grid);
    }

    public void setActivityDates(Set<LocalDate> dates) {
        this.activityDates = (dates == null) ? Collections.emptySet() : dates;
        populateCalendar(currentYearMonth);
    }

    public void setSelectedDate(LocalDate date) {
        if (date != null) {
            this.selectedDate = date;
            this.currentYearMonth = YearMonth.from(date);
            populateCalendar(currentYearMonth);
        }
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        calenderTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnPrev = new Button("‹");
        Button btnNext = new Button("›");
        String navBtnStyle = 
            "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-weight: bold; " +
            "-fx-font-size: 15px; -fx-background-radius: 6; -fx-padding: 2 10; -fx-cursor: hand;";
        btnPrev.setStyle(navBtnStyle);
        btnNext.setStyle(navBtnStyle);

        btnPrev.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            populateCalendar(currentYearMonth);
        });

        btnNext.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            populateCalendar(currentYearMonth);
        });

        HBox btnBox = new HBox(6, btnPrev, btnNext);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(calenderTitle, spacer, btnBox);
        return header;
    }

    private void populateCalendar(YearMonth yearMonth) {
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        calenderTitle.setText(monthName + " " + yearMonth.getYear());

        grid.getChildren().clear();

        // 1. Day of Week Column Headers (Sun -> Sat)
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int col = 0; col < 7; col++) {
            Label dayHeader = new Label(days[col]);
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setPrefWidth(36);
            dayHeader.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-weight: bold;");
            grid.add(dayHeader, col, 0);
        }

        LocalDate firstDay = yearMonth.atDay(1);
        // Convert DayOfWeek (Mon=1 ... Sun=7) to Sun=0 ... Sat=6
        int col = firstDay.getDayOfWeek().getValue() % 7;
        int row = 1;

        // 2. Day Cell Render Loop
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate dateForCell = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day);
            StackPane cell = createDayCell(dateForCell, day);

            grid.add(cell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private StackPane createDayCell(LocalDate date, int dayNumber) {
        Label dateLabel = new Label(String.valueOf(dayNumber));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: normal;");

        Circle dot = new Circle(2.5);
        boolean hasActivity = activityDates.contains(date);
        dot.setVisible(hasActivity);
        dot.setManaged(hasActivity);
        dot.setStyle("-fx-fill: #0B5FFF;");

        VBox content = new VBox(2, dateLabel, dot);
        content.setAlignment(Pos.CENTER);

        StackPane cell = new StackPane(content);
        cell.setPrefSize(36, 36);
        cell.setAlignment(Pos.CENTER);
        cell.setStyle("-fx-cursor: hand; -fx-background-radius: 8;");

        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);

        // Styling hierarchy: Selected > Today > Normal
        if (isSelected) {
            cell.setStyle("-fx-background-color: #0B5FFF; -fx-background-radius: 8; -fx-cursor: hand;");
            dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
            dot.setStyle("-fx-fill: white;");
        } else if (isToday) {
            cell.setStyle(
                "-fx-background-color: #EBF3FF; -fx-border-color: #93C5FD; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"
            );
            dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0B5FFF;");
        } else {
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
            // Subtle hover feedback
            cell.setOnMouseEntered(e -> cell.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-cursor: hand;"));
            cell.setOnMouseExited(e -> cell.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;"));
        }

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            populateCalendar(currentYearMonth);
            if (dateSelected != null) {
                dateSelected.accept(date);
            }
        });

        return cell;
    }
}
