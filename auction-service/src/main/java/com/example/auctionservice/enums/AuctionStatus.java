package com.example.auctionservice.enums;

public enum AuctionStatus {
    SCHEDULED("Coming Soon"),
    LIVE("Start Bidding"),
    OVER("Sold Out");
    public final String label;
    private AuctionStatus(String label) {
        this.label = label;
    }
}
