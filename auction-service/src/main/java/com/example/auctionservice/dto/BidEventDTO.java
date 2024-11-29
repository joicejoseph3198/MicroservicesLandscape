package com.example.auctionservice.dto;

import java.time.LocalDateTime;

public record BidEventDTO<T>(String eventType, T data, LocalDateTime timestamp) {
}
