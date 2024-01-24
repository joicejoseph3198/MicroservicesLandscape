package com.example.productcompositeservice.enums;

public enum Category {
    FULLSIZE("Full Size/100 percent"),
    TKL("TKL/TenKeyLess"),
    COMPACT("60 percent"),

    SEVENTYFIVE("75 percent"),

    SIXTYFIVE("65 percent"),

    FOURTY("40 percent"),

    SPLIT("Split"),

    MACRO("MacroPad");

    private String value;

    private Category(String value) {
        this.value = value;
    }
}
