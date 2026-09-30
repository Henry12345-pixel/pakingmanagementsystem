package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.ParkingSlotDAO;
import com.ucc.parkingsystem.model.ParkingSlot;
import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {

    // Each name must match an fx:id in admin-dashboard-view.fxml
    @FXML private Label welcomeLabel;
    @FXML private Label totalLabel;
    @FXML private Label availableLabel;
    @FXML private Label occupiedLabel;

    @FXML private TableView<ParkingSlot> slotsTable;
    @FXML private TableColumn<ParkingSlot, String> slotNumberColumn;
    @FXML private TableColumn<ParkingSlot, String> typeColumn;
    @FXML private TableColumn<ParkingSlot, String> statusColumn;
    @FXML private TableColumn<ParkingSlot, String> vehicleColumn;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        welcomeLabel.setText("Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");

        // Tell each column which value from a ParkingSlot to show.
        // "data.getValue()" is the ParkingSlot for that row.
        slotNumberColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getSlotNumber()));
        typeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTypeName()));
        statusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStatus()));
        vehicleColumn.setCellValueFactory(data -> {
            String plate = data.getValue().getPlateNumber();
            return new SimpleStringProperty(plate == null ? "-" : plate);
        });

        loadData();
        startAutoRefresh();
    }

    // Reads the slots from the database, then updates the cards and the table.
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

    // Calls loadData() every 5 seconds while this screen is showing.
    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            // If this screen was replaced (Logout, or opening another screen), stop the timer.
            if (slotsTable.getScene() == null || slotsTable.getScene().getWindow() == null) {
                autoRefresh.stop();
                return;
            }
            loadData();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    @FXML
    private void onRefreshClick() {
        loadData();
    }

    @FXML
    private void onLogoutClick() {
        Navigator.logout(welcomeLabel);
    }

    // The three screens below do not exist yet, so we show a message for now.
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
            Navigator.goToAdminOnly(welcomeLabel, "manage-slots-view.fxml", "Manage Parking Slots", 800, 550);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRecordsClick() {
        showComingSoon("Parking Records");
    }

    private void showComingSoon(String screenName) {

    }
    @FXML private Label lastUpdatedLabel;

    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Timeline autoRefresh;
}