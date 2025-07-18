package com.oriontek.customermanagement.enums;

/**
 * Enum que define los tipos de direcciones disponibles.
 */
public enum AddressType {
    HOME("Casa"),
    WORK("Trabajo"),
    BILLING("Facturación"),
    SHIPPING("Envío"),
    OTHER("Otra");

    private final String displayName;

    AddressType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}