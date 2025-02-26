package com.example.auctionservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.BidRequestDTO;

public interface BidService {
    ResponseDTO<String> placeBid(BidRequestDTO bidRequestDTO);

    void processBid(BidRequestDTO request);
}
