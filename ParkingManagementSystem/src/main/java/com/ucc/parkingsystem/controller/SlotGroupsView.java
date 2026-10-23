package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.model.ParkingSlot;
import com.ucc.parkingsystem.util.Ui;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

// The slot list on both dashboards: one collapsible "dropdown" section per vehicle type.
// Each section's title bar shows how many slots are available / occupied, and opening it
// shows that type's slots in a table. The sections are built once and only their contents
// are refreshed, so the auto-refresh doesn't collapse them or jump the scroll position.
public class SlotGroupsView {

    public static final String ALL = "All";

    private static final double ROW_HEIGHT = 52;
    private static final double HEADER_HEIGHT = 46;
    private static final int MIN_ROWS = 2;
    private static final int MAX_ROWS = 12;

    private final VBox container;
    private final Consumer<ParkingSlot> onExit;   // null = no Exit button (Admin view)
    private final Map<String, Section> sections = new LinkedHashMap<>();

    public SlotGroupsView(VBox container, Consumer<ParkingSlot> onExit) {
        this.container = container;
        this.onExit = onExit;
    }

    // Creates one (collapsed) section per vehicle type.
    public void setTypes(List<String> typeNames) {
        sections.clear();
        container.getChildren().clear();
        for (String typeName : typeNames) {
            Section section = new Section(typeName);
            sections.put(typeName, section);
            container.getChildren().add(section.pane);
        }
    }

    // Fills every section from the full slot list, applying the three filters.
    public void update(List<ParkingSlot> allSlots, String typeFilter, String floorFilter, String statusFilter) {
        for (Section section : sections.values()) {
            int available = 0;
            int occupied = 0;
            List<ParkingSlot> shown = new ArrayList<>();

            for (ParkingSlot slot : allSlots) {
                if (!section.typeName.equals(slot.getTypeName())) {
                    continue;
                }
                if (slot.isOccupied()) {
                    occupied++;
                } else {
                    available++;
                }
                boolean floorOk = ALL.equals(floorFilter) || Objects.equals(slot.getFloorLevel(), floorFilter);
                boolean statusOk = ALL.equals(statusFilter) || Objects.equals(slot.getStatus(), statusFilter);
                if (floorOk && statusOk) {
                    shown.add(slot);
                }
            }

            boolean typeOk = ALL.equals(typeFilter) || section.typeName.equals(typeFilter);
            section.pane.setVisible(typeOk);
            section.pane.setManaged(typeOk);

            section.availableBadge.setText(available + " available");
            section.occupiedBadge.setText(occupied + " occupied");
            section.setSlots(shown);
        }
    }

    public void setAllExpanded(boolean expanded) {
        for (Section section : sections.values()) {
            if (section.pane.isVisible()) {
                section.pane.setExpanded(expanded);
            }
        }
    }

    private static String formatFloor(ParkingSlot slot) {
        return Ui.titleCase(slot.getFloorLevel());
    }

    // ---------------------------------------------------------------------------------

    private final class Section {
        private final String typeName;
        private final TitledPane pane = new TitledPane();
        private final TableView<ParkingSlot> table = new TableView<>();
        private final Label availableBadge = new Label();
        private final Label occupiedBadge = new Label();
        private String lastSignature = "";

        Section(String typeName) {
            this.typeName = typeName;

            Label name = new Label(typeName);
            name.getStyleClass().add("group-title");
            availableBadge.getStyleClass().addAll("badge", "badge-available");
            occupiedBadge.getStyleClass().addAll("badge", "badge-occupied");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox header = new HBox(24, name, spacer, availableBadge, occupiedBadge);
            header.setAlignment(Pos.CENTER_LEFT);
            // Make the title row fill the whole bar (minus the arrow) so the badges sit at the right edge.
            header.prefWidthProperty().bind(pane.widthProperty().subtract(72));

            pane.setText(typeName);
            pane.setGraphic(header);
            pane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            pane.setExpanded(false);
            pane.setAnimated(false);

            buildTable();
            pane.setContent(table);
            setSlots(List.of());
        }

        private void buildTable() {
            TableColumn<ParkingSlot, String> slotColumn = new TableColumn<>("Slot");
            slotColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSlotNumber()));

            TableColumn<ParkingSlot, String> floorColumn = new TableColumn<>("Floor");
            floorColumn.setCellValueFactory(d -> new SimpleStringProperty(formatFloor(d.getValue())));

            TableColumn<ParkingSlot, String> statusColumn = new TableColumn<>("Status");
            statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
            statusColumn.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setGraphic(null);
                        return;
                    }
                    Label pill = new Label(Ui.titleCase(status));
                    pill.getStyleClass().addAll("badge",
                            "OCCUPIED".equals(status) ? "badge-occupied" : "badge-available");
                    setGraphic(pill);
                }
            });

            TableColumn<ParkingSlot, String> vehicleColumn = new TableColumn<>("Plate number");
            vehicleColumn.setCellValueFactory(d -> {
                String plate = d.getValue().getPlateNumber();
                return new SimpleStringProperty(plate == null ? "-" : plate);
            });

            table.getColumns().add(slotColumn);
            table.getColumns().add(floorColumn);
            table.getColumns().add(statusColumn);
            table.getColumns().add(vehicleColumn);

            if (onExit != null) {
                table.getColumns().add(buildActionColumn());
            }

            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setFixedCellSize(ROW_HEIGHT);
            table.setPlaceholder(new Label("No slots match the current filters."));
        }

        // Shows an Exit button, only on occupied rows.
        private TableColumn<ParkingSlot, Void> buildActionColumn() {
            TableColumn<ParkingSlot, Void> actionColumn = new TableColumn<>("Action");
            actionColumn.setSortable(false);
            actionColumn.setCellFactory(col -> new TableCell<>() {
                private final Button exitButton = new Button("Exit");

                {
                    exitButton.getStyleClass().addAll("button-danger", "button-small");
                    exitButton.setOnAction(event -> {
                        int index = getIndex();
                        if (index >= 0 && index < getTableView().getItems().size()) {
                            onExit.accept(getTableView().getItems().get(index));
                        }
                    });
                    setAlignment(Pos.CENTER_LEFT);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                        return;
                    }
                    ParkingSlot slot = getTableView().getItems().get(getIndex());
                    setGraphic(slot.isOccupied() ? exitButton : null);
                }
            });
            return actionColumn;
        }

        // Only touches the table when something actually changed, so the 5-second
        // refresh doesn't reset hover/scroll when nobody parked or left.
        void setSlots(List<ParkingSlot> slots) {
            StringBuilder signature = new StringBuilder();
            for (ParkingSlot slot : slots) {
                signature.append(slot.getSlotId()).append('|')
                        .append(slot.getSlotNumber()).append('|')
                        .append(slot.getFloorLevel()).append('|')
                        .append(slot.getStatus()).append('|')
                        .append(slot.getPlateNumber()).append(';');
            }
            String current = signature.toString();
            if (!current.equals(lastSignature)) {
                lastSignature = current;
                table.setItems(FXCollections.observableArrayList(slots));
            }

            // Size the table to its rows so the page scrolls as a whole instead of
            // each section having its own scrollbar (up to MAX_ROWS rows).
            int rows = Math.max(MIN_ROWS, Math.min(slots.size(), MAX_ROWS));
            double height = HEADER_HEIGHT + rows * ROW_HEIGHT + 2;
            table.setPrefHeight(height);
            table.setMinHeight(Region.USE_PREF_SIZE);
            table.setMaxHeight(Region.USE_PREF_SIZE);
        }
    }
}
