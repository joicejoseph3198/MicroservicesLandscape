package com.example.auction_consumer.enums;

public enum BidEventType {
    CONNECTION_ESTABLISHED,
    CONNECTION_DISRUPTED,
    BID_FAILED,
    NEW_HIGHEST_BID,
    BID_ACCEPTED,
    AUCTION_OVER,
    HEART_BEAT,
    AUCTION_WON,
}
