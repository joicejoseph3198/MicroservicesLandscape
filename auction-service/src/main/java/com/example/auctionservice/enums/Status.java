package com.example.auctionservice.enums;

public enum Status {
    UNPUBLISHED("Product Unavailable"),
    PUBLISHED("Coming Soon"),
    SCHEDULED("Coming Soon"),
    LIVE("Start Bidding"),
    SOLD("Sold Out"),
    UNSOLD("Product Unavailable");
    public final String label;
    private Status(String label) {
        this.label = label;
    }
}
