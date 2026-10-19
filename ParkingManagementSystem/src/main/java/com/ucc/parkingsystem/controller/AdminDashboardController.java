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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardController {

    private static final String ALL = "All";

    @FXML private Label welcomeLabel;
    @FXML private Label totalLabel;
    @FXML private Label availableLabel;
    @FXML private Label occupiedLabel;
    @FXML private Label lastUpdatedLabel;

    @FXML private ChoiceBox<String> typeFilterChoiceBox;
    @FXML private ChoiceBox<String> floorFilterChoiceBox;
    @FXML private ChoiceBox<String> statusFilterChoiceBox;

    @FXML private TableView<ParkingSlot> slotsTable;
    @FXML private TableColumn<ParkingSlot, String> slotNumberColumn;
    @FXML private TableColumn<ParkingSlot, String> typeColumn;
    @FXML private TableColumn<ParkingSlot, String> floorColumn;
    @FXML private TableColumn<ParkingSlot, String> statusColumn;
    @FXML private TableColumn<ParkingSlot, String> vehicleColumn;

    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Timeline autoRefresh;

    private List<ParkingSlot> allSlots = new ArrayList<>();

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

        setUpFilters();
        loadData();
        startAutoRefresh();
    }

    private void setUpFilters() {
        List<String> types = new ArrayList<>();
        types.add(ALL);
        try {
            types.addAll(ParkingSlotDAO.getAllVehicleTypeNames());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        typeFilterChoiceBox.setItems(FXCollections.observableArrayList(types));
        typeFilterChoiceBox.setValue(ALL);

        floorFilterChoiceBox.setItems(FXCollections.observableArrayList(ALL, "GROUND", "UPPER", "LOWER"));
        floorFilterChoiceBox.setValue(ALL);

        statusFilterChoiceBox.setItems(FXCollections.observableArrayList(ALL, "AVAILABLE", "OCCUPIED"));
        statusFilterChoiceBox.setValue(ALL);

        typeFilterChoiceBox.setOnAction(event -> applyFilters());
        floorFilterChoiceBox.setOnAction(event -> applyFilters());
        statusFilterChoiceBox.setOnAction(event -> applyFilters());
    }

    @FXML
    private void onClearFiltersClick() {
        typeFilterChoiceBox.setValue(ALL);
        floorFilterChoiceBox.setValue(ALL);
        statusFilterChoiceBox.setValue(ALL);
        applyFilters();
    }

    private void applyFilters() {
        String type = typeFilterChoiceBox.getValue();
        String floor = floorFilterChoiceBox.getValue();
        String status = statusFilterChoiceBox.getValue();

        List<ParkingSlot> matches = new ArrayList<>();
        for (ParkingSlot slot : allSlots) {
            boolean typeOk = ALL.equals(type) || slot.getTypeName().equals(type);
            boolean floorOk = ALL.equals(floor) || slot.getFloorLevel().equals(floor);
            boolean statusOk = ALL.equals(status) || slot.getStatus().equals(status);
            if (typeOk && floorOk && statusOk) {
                matches.add(slot);
            }
        }
        slotsTable.setItems(FXCollections.observableArrayList(matches));
    }

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