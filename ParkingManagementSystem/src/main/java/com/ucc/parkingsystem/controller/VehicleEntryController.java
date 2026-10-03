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

    // The dropdowns show text, so we remember the real IDs behind that text.
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

        floorChoiceBox.setItems(FXCollections.observableArrayList("GROUND", "UPPER", "LOWER"));

        // Reload slots whenever EITHER the type OR the floor changes.
        typeChoiceBox.setOnAction(event -> loadSlotsForSelection());
        floorChoiceBox.setOnAction(event -> loadSlotsForSelection());
    }

    private void loadSlotsForSelection() {
        String type = typeChoiceBox.getValue();
        String floor = floorChoiceBox.getValue();
        slotChoiceBox.getItems().clear();
        slotIdsByNumber.clear();

        if (type == null || floor == null) {
            return;   // wait until both are chosen
        }

        try {
            List<ParkingSlot> slots = ParkingSlotDAO.getAvailableSlots(type, floor);

            if (slots.isEmpty()) {
                showError("No available " + type + " slots on the " + floor.toLowerCase() + " floor.");
                return;
            }
            messageLabel.setText("");

            for (ParkingSlot slot : slots) {
                slotChoiceBox.getItems().add(slot.getSlotNumber());
                slotIdsByNumber.put(slot.getSlotNumber(), slot.getSlotId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Could not load available slots.");
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
            } else {
                showError("Slot " + slotNumber + " is no longer available. Please choose another.");
            }

            slotChoiceBox.setValue(null);
            String message = messageLabel.getText();
            String style = messageLabel.getStyle();
            loadSlotsForSelection();
            if (!message.isEmpty() && messageLabel.getText().isEmpty()) {
                messageLabel.setStyle(style);
                messageLabel.setText(message);
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