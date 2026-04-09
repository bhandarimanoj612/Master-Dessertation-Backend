package com.example.sajilo_tayaar.entity.enums;


public enum Role {
    PLATFORM_ADMIN,   // creates shops (tenants), assigns owners, manages users platform-wide
    SHOP_OWNER,       // manages their shop, staff, inventory, POS, technicians
    SHOP_STAFF,       // counter staff: create bookings, billing, inventory sales
    TECHNICIAN,       // updates repair status, notes, job progress
    CUSTOMER          // creates booking, checks status
}

