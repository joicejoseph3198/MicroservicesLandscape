package com.example.auctionservice.dto;

import java.math.BigDecimal;

public record AuctionDetailsDTO(Long id, String productSkuCode, String startTime, String endTime,
                                BigDecimal bidStartPrice, BigDecimal buyNowPrice,String auctionStatus,
                                BigDecimal highestBid) {
}
