package com.example.productservice.enums;

public enum SortType {
    PRICE_DESC("-1"),
    PRICE_ASC("1");

    private final String value;
    private SortType(String value) {
        this.value = value;
    }
}
