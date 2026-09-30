package com.ucc.parkingsystem.model;

public class ParkingRecord {
    private final int recordId;
    private final String plateNumber;
    private final String typeName;
    private final String slotNumber;
    private final String entryTime;
    private final String exitTime;    // null while still parked

    public ParkingRecord(int recordId, String plateNumber, String typeName,
                         String slotNumber, String entryTime, String exitTime) {
        this.recordId = recordId;
        this.plateNumber = plateNumber;
        this.typeName = typeName;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
    }

    public int getRecordId() { return recordId; }
    public String getPlateNumber() { return plateNumber; }
    public String getTypeName() { return typeName; }
    public String getSlotNumber() { return slotNumber; }
    public String getEntryTime() { return entryTime; }
    public String getExitTime() { return exitTime; }
}