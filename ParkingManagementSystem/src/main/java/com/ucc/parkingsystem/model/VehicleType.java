package com.ucc.parkingsystem.model;

public class VehicleType {
    private final int typeId;
    private final String typeName;

    public VehicleType(int typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public int getTypeId() { return typeId; }
    public String getTypeName() { return typeName; }
}