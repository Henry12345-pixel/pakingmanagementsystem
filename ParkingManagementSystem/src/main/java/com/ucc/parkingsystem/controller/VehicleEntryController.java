package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.ParkingRecordDAO;
import com.ucc.parkingsystem.database.ParkingSlotDAO;
import com.ucc.parkingsystem.database.VehicleTypeDAO;
import com.ucc.parkingsystem.model.ParkingSlot;
import com.ucc.parkingsystem.model.VehicleType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleEntryController {

    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private ChoiceBox<String> floorChoiceBox;
    @FXML private ChoiceBox<String> slotChoiceBox;
    @FXML private TextField plateField;
    @FXML private Label messageLabel;

    private final Map<String, Integer> typeIdsByName = new HashMap<>();
    private final Map<String, Integer> slotIdsByNumber = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            List<VehicleType> types = VehicleTypeDAO.getAllTypes();
            for (VehicleType type : types) {
                typeChoiceBox.getItems().add(type.getTypeName());
                typeIdsByName.put(type.getTypeName(), type.getTypeId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Could not load vehicle types.");
        }

        typeChoiceBox.setOnAction(event -> {
            loadFloorsForSelectedType();
            loadSlotsForSelection(true);
        });
        floorChoiceBox.setOnAction(event -> loadSlotsForSelection(true));
    }

    private void loadFloorsForSelectedType() {
        String type = typeChoiceBox.getValue();
        List<String> allowed = VehicleFloorRules.allowedFloors(type);
        floorChoiceBox.setItems(FXCollections.observableArrayList(allowed));
        floorChoiceBox.setValue(allowed.size() == 1 ? allowed.get(0) : null);
    }

    // announceEmpty: whether to show a warning when no slots are available.
    // We pass false right after a successful entry, so this silent refresh
    // doesn't overwrite the green "Recorded" success message.
    private void loadSlotsForSelection(boolean announceEmpty) {
        String type = typeChoiceBox.getValue();
        String floor = floorChoiceBox.getValue();
        slotChoiceBox.getItems().clear();
        slotIdsByNumber.clear();

        if (type == null || floor == null) {
            return;
        }

        try {
            List<ParkingSlot> slots = ParkingSlotDAO.getAvailableSlots(type, floor);

            if (slots.isEmpty()) {
                if (announceEmpty) {
                    showError("No available " + type + " slots on the " + floor.toLowerCase() + " floor.");
                }
                return;
            }

            if (announceEmpty) {
                messageLabel.setText("");
            }

            for (ParkingSlot slot : slots) {
                slotChoiceBox.getItems().add(slot.getSlotNumber());
                slotIdsByNumber.put(slot.getSlotNumber(), slot.getSlotId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (announceEmpty) {
                showError("Could not load available slots.");
            }
        }
    }

    @FXML
    private void onRecordEntryClick() {
        String type = typeChoiceBox.getValue();
        String floor = floorChoiceBox.getValue();
        String slotNumber = slotChoiceBox.getValue();
        String plate = plateField.getText().trim();

        if (type == null) {
            showError("Choose a vehicle type.");
            return;
        }
        if (floor == null) {
            showError("Choose a floor.");
            return;
        }
        if (slotNumber == null) {
            showError("Choose an available slot.");
            return;
        }
        if (plate.isEmpty()) {
            showError("Enter the plate number.");
            return;
        }

        int typeId = typeIdsByName.get(type);
        int slotId = slotIdsByNumber.get(slotNumber);

        try {
            boolean recorded = ParkingRecordDAO.recordEntry(plate, typeId, slotId);

            if (recorded) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Recorded: " + plate + " parked at " + slotNumber + ".");
                plateField.clear();
                slotChoiceBox.setValue(null);
                loadSlotsForSelection(false);   // quiet refresh, keeps the success message visible
            } else {
                slotChoiceBox.setValue(null);
                loadSlotsForSelection(true);
                showError("Slot " + slotNumber + " is no longer available. Please choose another.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Could not record the entry. Please try again.");
        }
    }

    @FXML
    private void onBackClick() {
        try {
            Navigator.goTo(plateField, "staff-dashboard-view.fxml", "Staff Dashboard", 900, 600);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }
}