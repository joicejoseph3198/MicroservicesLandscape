package com.example.auctionservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.Status;

public interface AuctionService {
    ResponseDTO<String> scheduleAuction(AuctionScheduleDTO request);
    ResponseDTO<Auction> findAuctionBySkuCode(String skuCode);
    ResponseDTO<String> updateStatus(Long auctionId, Status status);
}