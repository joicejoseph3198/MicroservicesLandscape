package com.example.auctionservice.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record AuctionScheduleDTO(String productSkuCode, String startTime, String endTime,
                                 BigDecimal bidStartPrice, BigDecimal buyNowPrice) {
}
