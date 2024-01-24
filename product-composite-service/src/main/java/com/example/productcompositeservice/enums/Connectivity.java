package com.example.productcompositeservice.enums;

public enum Connectivity {
    BLUETOOTH("Bluetooth"),
    WIRED("Wired"),
    DONGLE("2.4 GHz");
    private final String value;

    private Connectivity(String value){
        this.value = value;
    }
}
