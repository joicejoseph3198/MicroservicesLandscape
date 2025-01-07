package com.example.auctionservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.AuctionDetailsDTO;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.AuctionStatus;
import org.springframework.web.bind.annotation.RequestParam;

public interface AuctionService {
    ResponseDTO<String> scheduleAuction(AuctionScheduleDTO request);
    ResponseDTO<String> endAuction(String skuCode);
    ResponseDTO<AuctionDetailsDTO> findAuctionBySkuCode(String skuCode);
    ResponseDTO<String> updateStatus(Long auctionId, AuctionStatus auctionStatus);
    void startScheduledAuction();
    void endScheduledAuction();
    void purgeStaleAuction();
}
