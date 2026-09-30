package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.ParkingRecordDAO;
import com.ucc.parkingsystem.model.ParkingRecord;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleExitController {

    @FXML private TextField searchField;
    @FXML private Label messageLabel;

    @FXML private TableView<ParkingRecord> parkedTable;
    @FXML private TableColumn<ParkingRecord, String> plateColumn;
    @FXML private TableColumn<ParkingRecord, String> typeColumn;
    @FXML private TableColumn<ParkingRecord, String> slotColumn;
    @FXML private TableColumn<ParkingRecord, String> entryColumn;

    // Every parked vehicle. The table shows only the ones that match the search.
    private List<ParkingRecord> allParked = new ArrayList<>();

    @FXML
    public void initialize() {
        plateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getPlateNumber()));
        typeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTypeName()));
        slotColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getSlotNumber()));
        entryColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getEntryTime()));

        // Every time the search text changes, filter the table again.
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilter());

        loadParked();
    }

    // Reads from the database, then shows the list.
    private void loadParked() {
        try {
            allParked = ParkingRecordDAO.getParkedVehicles();
            applyFilter();
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Could not load parked vehicles.", false);
        }
    }

    // Keeps only the vehicles whose plate contains the search text.
    private void applyFilter() {
        String search = searchField.getText().trim().toLowerCase();
        List<ParkingRecord> matches = new ArrayList<>();

        for (ParkingRecord record : allParked) {
            if (record.getPlateNumber().toLowerCase().contains(search)) {
                matches.add(record);
            }
        }
        parkedTable.setItems(FXCollections.observableArrayList(matches));
    }

    @FXML
    private void onRecordExitClick() {
        ParkingRecord selected = parkedTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Select a vehicle from the table first.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Record exit for " + selected.getPlateNumber()
                        + " (slot " + selected.getSlotNumber() + ")?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }


        try {
            boolean recorded = ParkingRecordDAO.recordExit(selected.getRecordId());

            if (recorded) {
                loadParked();   // reload first, because it doesn't touch the message
                showMessage(selected.getPlateNumber() + " has exited. Slot "
                        + selected.getSlotNumber() + " is now available.", true);
            } else {
                loadParked();
                showMessage("This exit was already recorded.", false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Could not record the exit. Please try again.", false);
        }
    }

    @FXML
    private void onBackClick() {
        try {
            Navigator.goTo(searchField, "staff-dashboard-view.fxml", "Staff Dashboard", 900, 600);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        messageLabel.setText(message);
    }
}