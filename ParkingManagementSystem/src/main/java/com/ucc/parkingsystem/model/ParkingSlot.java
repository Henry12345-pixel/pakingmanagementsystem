package com.ucc.parkingsystem.model;

public class ParkingSlot {
    private final int slotId;
    private final String slotNumber;
    private final String typeName;
    private final String floorLevel;     // new: "GROUND", "UPPER", or "LOWER"
    private final String status;
    private final String plateNumber;

    public ParkingSlot(int slotId, String slotNumber, String typeName, String floorLevel,
                       String status, String plateNumber) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.typeName = typeName;
        this.floorLevel = floorLevel;
        this.status = status;
        this.plateNumber = plateNumber;
    }

    public int getSlotId() { return slotId; }
    public String getSlotNumber() { return slotNumber; }
    public String getTypeName() { return typeName; }
    public String getFloorLevel() { return floorLevel; }
    public String getStatus() { return status; }
    public String getPlateNumber() { return plateNumber; }

    public boolean isOccupied() {
        return status.equals("OCCUPIED");
    }
}