package com.ucc.parkingsystem.model;

// One parking slot, plus the plate of the vehicle in it (if any).
public class ParkingSlot {
    private final int slotId;
    private final String slotNumber;
    private final String typeName;      // "2-Wheel", "3-Wheel" or "4-Wheel"
    private final String status;        // "AVAILABLE" or "OCCUPIED"
    private final String plateNumber;   // null when the slot is empty

    public ParkingSlot(int slotId, String slotNumber, String typeName,
                       String status, String plateNumber) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.typeName = typeName;
        this.status = status;
        this.plateNumber = plateNumber;
    }

    public int getSlotId() { return slotId; }
    public String getSlotNumber() { return slotNumber; }
    public String getTypeName() { return typeName; }
    public String getStatus() { return status; }
    public String getPlateNumber() { return plateNumber; }

    public boolean isOccupied() {
        return status.equals("OCCUPIED");
    }
}