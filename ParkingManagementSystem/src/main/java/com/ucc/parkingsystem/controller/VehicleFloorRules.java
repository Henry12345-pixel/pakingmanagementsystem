package com.ucc.parkingsystem.controller;

import java.util.List;

// Business rule: which floors are valid for each vehicle type.
public class VehicleFloorRules {

    public static final String TYPE_2WHEEL_ABOVE = "2-Wheel (Above 400cc)";
    public static final String TYPE_2WHEEL_BELOW = "2-Wheel (Below 400cc)";
    public static final String TYPE_3WHEEL = "3-Wheel";
    public static final String TYPE_4WHEEL = "4-Wheel";

    public static List<String> allowedFloors(String typeName) {
        if (TYPE_2WHEEL_ABOVE.equals(typeName)) {
            return List.of("UPPER", "LOWER");
        }
        if (TYPE_2WHEEL_BELOW.equals(typeName)) {
            return List.of("GROUND");
        }
        if (TYPE_3WHEEL.equals(typeName)) {
            return List.of("GROUND");
        }
        if (TYPE_4WHEEL.equals(typeName)) {
            return List.of("UPPER", "LOWER");
        }
        return List.of("GROUND", "UPPER", "LOWER");   // fallback for any future/unknown type
    }
}