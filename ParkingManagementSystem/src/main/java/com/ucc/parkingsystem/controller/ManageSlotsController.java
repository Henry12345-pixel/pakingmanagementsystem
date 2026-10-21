package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.ParkingSlotDAO;
import com.ucc.parkingsystem.model.ParkingSlot;
import com.ucc.parkingsystem.util.Ui;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

public class ManageSlotsController {

    @FXML private TextField slotNumberField;
    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private ChoiceBox<String> floorChoiceBox;
    @FXML private Label messageLabel;

    @FXML private TableView<ParkingSlot> slotsTable;
    @FXML private TableColumn<ParkingSlot, String> slotNumberColumn;
    @FXML private TableColumn<ParkingSlot, String> typeColumn;
    @FXML private TableColumn<ParkingSlot, String> floorColumn;
    @FXML private TableColumn<ParkingSlot, String> statusColumn;
    @FXML private TableColumn<ParkingSlot, Void> actionColumn;

    @FXML
    public void initialize() {
        slotNumberColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getSlotNumber()));
        typeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTypeName()));
        floorColumn.setCellValueFactory(
                data -> new SimpleStringProperty(Ui.titleCase(data.getValue().getFloorLevel())));
        statusColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getStatus()));

        setUpActionColumn();

        floorChoiceBox.setConverter(Ui.titleCaseConverter());

        // Floor choices depend on which vehicle type is selected.
        typeChoiceBox.setOnAction(event -> loadFloorsForSelectedType());

        loadTypes();
        loadSlots();
    }

    // The Delete button at the right end of every slot row.
    // It is greyed out for occupied slots, which can't be deleted.
    private void setUpActionColumn() {
        actionColumn.setSortable(false);
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.getStyleClass().addAll("button-danger", "button-small");
                deleteButton.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        onDeleteClick(getTableView().getItems().get(index));
                    }
                });
                setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                ParkingSlot slot = getTableView().getItems().get(getIndex());
                deleteButton.setDisable(slot.isOccupied());
                setGraphic(deleteButton);
            }
        });
    }

    // Rebuilds the floor dropdown based on the chosen vehicle type.
    private void loadFloorsForSelectedType() {
        String type = typeChoiceBox.getValue();
        List<String> allowed = VehicleFloorRules.allowedFloors(type);
        floorChoiceBox.setItems(FXCollections.observableArrayList(allowed));
        floorChoiceBox.setValue(allowed.size() == 1 ? allowed.get(0) : null);
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
        String floor = floorChoiceBox.getValue();

        if (slotNumber.isEmpty() || type == null || floor == null) {
            Ui.error(messageLabel, "Enter a slot number, vehicle type, and floor.");
            return;
        }

        try {
            ParkingSlotDAO.addSlot(slotNumber, type, floor);
            onClearClick();
            loadSlots();
            Ui.success(messageLabel, "Slot " + slotNumber + " added.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                Ui.error(messageLabel, "Slot number \"" + slotNumber + "\" already exists.");
            } else {
                showError("Could not add the slot.", e);
            }
        }
    }

    // Called by the Delete button on a row.
    private void onDeleteClick(ParkingSlot slot) {
        if (slot.isOccupied()) {
            Ui.error(messageLabel, "Cannot delete an occupied slot.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete slot " + slot.getSlotNumber() + "?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        try {
            ParkingSlotDAO.deleteSlot(slot.getSlotId());
            loadSlots();
            Ui.success(messageLabel, "Slot " + slot.getSlotNumber() + " deleted.");
        } catch (SQLException e) {
            Ui.error(messageLabel, "Slot " + slot.getSlotNumber()
                    + " has parking history and cannot be deleted.");
        }
    }

    @FXML
    private void onClearClick() {
        slotNumberField.clear();
        typeChoiceBox.setValue(null);
        floorChoiceBox.setItems(FXCollections.observableArrayList());
        floorChoiceBox.setValue(null);
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
        Ui.error(messageLabel, message);
    }
}