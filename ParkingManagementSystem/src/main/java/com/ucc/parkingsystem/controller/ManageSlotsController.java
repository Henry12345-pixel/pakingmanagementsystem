package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.ParkingSlotDAO;
import com.ucc.parkingsystem.model.ParkingSlot;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class ManageSlotsController {

    @FXML private TextField slotNumberField;
    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private Label messageLabel;

    @FXML private TableView<ParkingSlot> slotsTable;
    @FXML private TableColumn<ParkingSlot, String> slotNumberColumn;
    @FXML private TableColumn<ParkingSlot, String> typeColumn;
    @FXML private TableColumn<ParkingSlot, String> statusColumn;

    // Remembers which slot is selected for Update/Delete, or null if none.
    private ParkingSlot selectedSlot;

    @FXML
    public void initialize() {
        slotNumberColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getSlotNumber()));
        typeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTypeName()));
        statusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStatus()));

        // When a table row is clicked, copy its values into the form.
        slotsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> {
            selectedSlot = newRow;
            if (newRow != null) {
                slotNumberField.setText(newRow.getSlotNumber());
                typeChoiceBox.setValue(newRow.getTypeName());
            }
        });

        loadTypes();
        loadSlots();
    }

    private void loadTypes() {
        try {
            List<String> types = ParkingSlotDAO.getAllVehicleTypeNames();
            typeChoiceBox.setItems(FXCollections.observableArrayList(types));
        } catch (SQLException e) {
            showError("Could not load vehicle types.", e);
        }
    }

    private void loadSlots() {
        try {
            List<ParkingSlot> slots = ParkingSlotDAO.getAllSlots();
            slotsTable.setItems(FXCollections.observableArrayList(slots));
        } catch (SQLException e) {
            showError("Could not load parking slots.", e);
        }
    }

    @FXML
    private void onAddClick() {
        String slotNumber = slotNumberField.getText().trim();
        String type = typeChoiceBox.getValue();

        if (slotNumber.isEmpty() || type == null) {
            messageLabel.setText("Enter a slot number and choose a vehicle type.");
            return;
        }

        try {
            ParkingSlotDAO.addSlot(slotNumber, type);
            messageLabel.setText("");
            onClearClick();
            loadSlots();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                messageLabel.setText("Slot number \"" + slotNumber + "\" already exists.");
            } else {
                showError("Could not add the slot.", e);
            }
        }
    }

    @FXML
    private void onUpdateClick() {
        if (selectedSlot == null) {
            messageLabel.setText("Select a slot from the table first.");
            return;
        }
        if (selectedSlot.isOccupied()) {
            messageLabel.setText("Cannot edit an occupied slot.");
            return;
        }

        String slotNumber = slotNumberField.getText().trim();
        String type = typeChoiceBox.getValue();

        if (slotNumber.isEmpty() || type == null) {
            messageLabel.setText("Enter a slot number and choose a vehicle type.");
            return;
        }

        try {
            ParkingSlotDAO.updateSlot(selectedSlot.getSlotId(), slotNumber, type);
            messageLabel.setText("");
            onClearClick();
            loadSlots();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                messageLabel.setText("Slot number \"" + slotNumber + "\" already exists.");
            } else {
                showError("Could not update the slot.", e);
            }
        }
    }

    @FXML
    private void onDeleteClick() {
        if (selectedSlot == null) {
            messageLabel.setText("Select a slot from the table first.");
            return;
        }
        if (selectedSlot.isOccupied()) {
            messageLabel.setText("Cannot delete an occupied slot.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete slot " + selectedSlot.getSlotNumber() + "?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        try {
            ParkingSlotDAO.deleteSlot(selectedSlot.getSlotId());
            messageLabel.setText("");
            onClearClick();
            loadSlots();
        } catch (SQLException e) {
            messageLabel.setText("This slot has parking history and cannot be deleted.");
        }
    }

    @FXML
    private void onClearClick() {
        slotNumberField.clear();
        typeChoiceBox.setValue(null);
        slotsTable.getSelectionModel().clearSelection();
        selectedSlot = null;
    }

    @FXML
    private void onBackClick() {
        try {
            Navigator.goToAdminOnly(slotNumberField, "admin-dashboard-view.fxml",
                    "Admin Dashboard", 900, 600);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message, SQLException e) {
        e.printStackTrace();
        messageLabel.setText(message);
    }
}