package com.example.auctionservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.BidRequestDTO;
import com.example.auctionservice.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auction/bid")
public class BidController {

    private final BidService bidService;

    @Autowired
    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping("/place-bid")
    ResponseDTO<String> placeBid(@RequestBody BidRequestDTO bidRequestDTO){
        return bidService.placeBid(bidRequestDTO);
    }
}
