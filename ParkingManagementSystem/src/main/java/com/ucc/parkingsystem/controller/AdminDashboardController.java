package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.ParkingSlotDAO;
import com.ucc.parkingsystem.model.ParkingSlot;
import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalLabel;
    @FXML private Label availableLabel;
    @FXML private Label occupiedLabel;
    @FXML private Label lastUpdatedLabel;

    @FXML private TableView<ParkingSlot> slotsTable;
    @FXML private TableColumn<ParkingSlot, String> slotNumberColumn;
    @FXML private TableColumn<ParkingSlot, String> typeColumn;
    @FXML private TableColumn<ParkingSlot, String> floorColumn;
    @FXML private TableColumn<ParkingSlot, String> statusColumn;
    @FXML private TableColumn<ParkingSlot, String> vehicleColumn;

    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        welcomeLabel.setText("Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");

        slotNumberColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getSlotNumber()));
        typeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTypeName()));
        floorColumn.setCellValueFactory(data -> {
            String floor = data.getValue().getFloorLevel();
            String formatted = floor.charAt(0) + floor.substring(1).toLowerCase();
            return new SimpleStringProperty(formatted);
        });
        statusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStatus()));
        vehicleColumn.setCellValueFactory(data -> {
            String plate = data.getValue().getPlateNumber();
            return new SimpleStringProperty(plate == null ? "-" : plate);
        });

        loadData();
        startAutoRefresh();
    }

    // Calls loadData() every 5 seconds while this screen is showing.
    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            if (slotsTable.getScene() == null || slotsTable.getScene().getWindow() == null) {
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
            List<ParkingSlot> slots = ParkingSlotDAO.getAllSlots();

            int total = slots.size();
            int occupied = 0;
            for (ParkingSlot slot : slots) {
                if (slot.isOccupied()) {
                    occupied++;
                }
            }

            totalLabel.setText(String.valueOf(total));
            occupiedLabel.setText(String.valueOf(occupied));
            availableLabel.setText(String.valueOf(total - occupied));
            slotsTable.setItems(FXCollections.observableArrayList(slots));

            lastUpdatedLabel.setStyle("-fx-text-fill: gray;");
            lastUpdatedLabel.setText("Last updated: " + LocalTime.now().format(CLOCK_FORMAT));

        } catch (SQLException e) {
            e.printStackTrace();
            lastUpdatedLabel.setStyle("-fx-text-fill: red;");
            lastUpdatedLabel.setText("Could not refresh data.");
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
    private void onManageStaffClick() {
        try {
            Navigator.goToAdminOnly(welcomeLabel, "manage-staff-view.fxml", "Manage Staff Accounts", 750, 550);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onManageSlotsClick() {
        try {
            Navigator.goToAdminOnly(welcomeLabel, "manage-slots-view.fxml", "Manage Parking Slots", 850, 550);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRecordsClick() {
        try {
            Navigator.goToAdminOnly(welcomeLabel, "parking-records-view.fxml", "Parking Records", 900, 600);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}