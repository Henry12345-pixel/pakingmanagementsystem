package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.ParkingRecordDAO;
import com.ucc.parkingsystem.database.ParkingSlotDAO;
import com.ucc.parkingsystem.model.ParkingSlot;
import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import com.ucc.parkingsystem.util.Ui;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StaffDashboardController {

    private static final String ALL = SlotGroupsView.ALL;

    @FXML private Label welcomeLabel;
    @FXML private Label totalLabel;
    @FXML private Label availableLabel;
    @FXML private Label occupiedLabel;
    @FXML private Label lastUpdatedLabel;

    @FXML private ChoiceBox<String> typeFilterChoiceBox;
    @FXML private ChoiceBox<String> floorFilterChoiceBox;
    @FXML private ChoiceBox<String> statusFilterChoiceBox;

    // Holds one collapsible section per vehicle type (see SlotGroupsView).
    @FXML private VBox groupsContainer;

    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Timeline autoRefresh;

    private SlotGroupsView groups;

    // Every slot from the database. The sections show only the ones matching the filters.
    private List<ParkingSlot> allSlots = new ArrayList<>();

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        welcomeLabel.setText("Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");

        List<String> typeNames = loadTypeNames();
        // Staff get an Exit button on occupied rows.
        groups = new SlotGroupsView(groupsContainer, this::handleExitClick);
        groups.setTypes(typeNames);

        setUpFilters(typeNames);
        loadData();
        startAutoRefresh();
    }

    private List<String> loadTypeNames() {
        try {
            return ParkingSlotDAO.getAllVehicleTypeNames();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void setUpFilters(List<String> typeNames) {
        List<String> types = new ArrayList<>();
        types.add(ALL);
        types.addAll(typeNames);
        typeFilterChoiceBox.setItems(FXCollections.observableArrayList(types));
        typeFilterChoiceBox.setValue(ALL);

        floorFilterChoiceBox.setItems(FXCollections.observableArrayList(ALL, "GROUND", "UPPER", "LOWER"));
        floorFilterChoiceBox.setValue(ALL);
        floorFilterChoiceBox.setConverter(Ui.titleCaseConverter());

        statusFilterChoiceBox.setItems(FXCollections.observableArrayList(ALL, "AVAILABLE", "OCCUPIED"));
        statusFilterChoiceBox.setValue(ALL);
        statusFilterChoiceBox.setConverter(Ui.titleCaseConverter());

        typeFilterChoiceBox.setOnAction(event -> onFilterChanged());
        floorFilterChoiceBox.setOnAction(event -> onFilterChanged());
        statusFilterChoiceBox.setOnAction(event -> onFilterChanged());
    }

    @FXML
    private void onClearFiltersClick() {
        typeFilterChoiceBox.setValue(ALL);
        floorFilterChoiceBox.setValue(ALL);
        statusFilterChoiceBox.setValue(ALL);
        applyFilters();
    }

    @FXML
    private void onExpandAllClick() {
        groups.setAllExpanded(true);
    }

    @FXML
    private void onCollapseAllClick() {
        groups.setAllExpanded(false);
    }

    // When the user picks a filter, open the sections so the matching slots are visible right away.
    private void onFilterChanged() {
        applyFilters();
        boolean filtering = !ALL.equals(typeFilterChoiceBox.getValue())
                || !ALL.equals(floorFilterChoiceBox.getValue())
                || !ALL.equals(statusFilterChoiceBox.getValue());
        if (filtering) {
            groups.setAllExpanded(true);
        }
    }

    private void applyFilters() {
        groups.update(allSlots,
                typeFilterChoiceBox.getValue(),
                floorFilterChoiceBox.getValue(),
                statusFilterChoiceBox.getValue());
    }

    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            if (groupsContainer.getScene() == null || groupsContainer.getScene().getWindow() == null) {
                autoRefresh.stop();
                return;
            }
            loadData();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void loadData() {
        try {
            allSlots = ParkingSlotDAO.getAllSlots();

            int total = allSlots.size();
            int occupied = 0;
            for (ParkingSlot slot : allSlots) {
                if (slot.isOccupied()) {
                    occupied++;
                }
            }

            totalLabel.setText(String.valueOf(total));
            occupiedLabel.setText(String.valueOf(occupied));
            availableLabel.setText(String.valueOf(total - occupied));

            applyFilters();

            Ui.muted(lastUpdatedLabel, "Last updated: " + LocalTime.now().format(CLOCK_FORMAT));

        } catch (SQLException e) {
            e.printStackTrace();
            Ui.error(lastUpdatedLabel, "Could not refresh data.");
        }
    }

    private void handleExitClick(ParkingSlot slot) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Record exit for " + slot.getPlateNumber() + " (slot " + slot.getSlotNumber() + ")?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        try {
            boolean exited = ParkingRecordDAO.recordExitBySlot(slot.getSlotId());
            loadData();

            if (!exited) {
                Ui.error(lastUpdatedLabel, "That vehicle had already exited.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Ui.error(lastUpdatedLabel, "Could not record the exit.");
        }
    }

    @FXML
    private void onRefreshClick() {
        loadData();
    }

    @FXML
    private void onLogoutClick() {
        Navigator.logout(welcomeLabel);
    }

    @FXML
    private void onRecordEntryClick() {
        try {
            Navigator.openPopup(welcomeLabel, "vehicle-entry-view.fxml", "Record Vehicle Entry");
            loadData();   // pick up whatever was recorded while the pop-up was open
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}