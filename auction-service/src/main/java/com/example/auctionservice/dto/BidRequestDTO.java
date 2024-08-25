package com.example.auctionservice.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record BidRequestDTO(String user, Long auctionId, BigDecimal amount, ZonedDateTime timestamp, Boolean buyNowTriggered) {
}
