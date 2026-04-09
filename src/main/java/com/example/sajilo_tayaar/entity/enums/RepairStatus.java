package com.example.sajilo_tayaar.entity.enums;

public enum RepairStatus {
    // The enum keeps the old repair flow and adds estimate + payment steps for the new journey.

    REQUESTED,
    ESTIMATE_PROVIDED,
    CUSTOMER_CONFIRMED,
    CONFIRMED,
    PICKUP_SCHEDULED,
    PICKED_UP,
    DROPPED_OFF,
    RECEIVED_AT_SHOP,
    DIAGNOSIS,
    IN_PROGRESS,
    WAITING_PARTS,
    READY,
    DELIVERING,
    DELIVERED,
    COMPLETED,
    PAID,
    CANCELLED
}
