package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.ParkingRecordDAO;
import com.ucc.parkingsystem.model.ParkingRecord;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParkingRecordsController {

    private static final String SHOW_ALL = "All";
    private static final String SHOW_PARKED = "Still parked";
    private static final String SHOW_EXITED = "Exited";

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> filterChoiceBox;
    @FXML private Label countLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<ParkingRecord> recordsTable;
    @FXML private TableColumn<ParkingRecord, String> plateColumn;
    @FXML private TableColumn<ParkingRecord, String> typeColumn;
    @FXML private TableColumn<ParkingRecord, String> slotColumn;
    @FXML private TableColumn<ParkingRecord, String> entryColumn;
    @FXML private TableColumn<ParkingRecord, String> exitColumn;

    // Every record from the database. The table shows only the ones that match the filters.
    private List<ParkingRecord> allRecords = new ArrayList<>();

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
        exitColumn.setCellValueFactory(data -> {
            String exit = data.getValue().getExitTime();
            return new SimpleStringProperty(exit == null ? "Still parked" : exit);
        });

        filterChoiceBox.setItems(FXCollections.observableArrayList(
                SHOW_ALL, SHOW_PARKED, SHOW_EXITED));
        filterChoiceBox.setValue(SHOW_ALL);

        // Re-filter whenever the search text or the filter choice changes.
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
        filterChoiceBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        loadRecords();
    }

    private void loadRecords() {
        try {
            allRecords = ParkingRecordDAO.getAllRecords();
            messageLabel.setText("");
            applyFilters();
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Could not load parking records.");
        }
    }

    // Keeps only the records that match BOTH the plate search and the status filter.
    private void applyFilters() {
        // Spaces are ignored on both sides, so "abc1234" finds "ABC 1234".
        String search = searchField.getText().replace(" ", "").toLowerCase();
        String filter = filterChoiceBox.getValue();
        List<ParkingRecord> matches = new ArrayList<>();

        for (ParkingRecord record : allRecords) {
            boolean stillParked = record.getExitTime() == null;

            boolean statusOk = SHOW_ALL.equals(filter)
                    || (SHOW_PARKED.equals(filter) && stillParked)
                    || (SHOW_EXITED.equals(filter) && !stillParked);

            boolean plateOk = record.getPlateNumber().replace(" ", "").toLowerCase().contains(search);

            if (statusOk && plateOk) {
                matches.add(record);
            }
        }

        recordsTable.setItems(FXCollections.observableArrayList(matches));
        countLabel.setText(matches.size() + " of " + allRecords.size() + " records");
    }

    @FXML
    private void onRefreshClick() {
        loadRecords();
    }

    @FXML
    private void onBackClick() {
        try {
            Navigator.goToAdminOnly(searchField, "admin-dashboard-view.fxml",
                    "Admin Dashboard", 900, 600);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}