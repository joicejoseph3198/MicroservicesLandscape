package com.example.auctionservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.BidRequestDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.entity.Bid;
import com.example.auctionservice.enums.AuctionStatus;
import com.example.auctionservice.enums.BidEventType;
import com.example.auctionservice.messaging.BidProducer;
import com.example.auctionservice.repository.AuctionRepository;
import com.example.auctionservice.repository.BidRepository;
import com.example.auctionservice.service.AuctionService;
import com.example.auctionservice.service.BidService;
import com.example.auctionservice.service.SseService;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final SseService notificationService;
    private final AuctionRepository auctionRepository;
    private final BidProducer bidProducer;
    private final AuctionService auctionService;

    @Autowired
    public BidServiceImpl(AuctionRepository auctionRepository, StreamBridge streamBridge, BidProducer bidProducer,
                          BidRepository bidRepository, SseService notificationService, AuctionService auctionService) {
        this.auctionRepository = auctionRepository;
        this.bidProducer = bidProducer;
        this.bidRepository = bidRepository;
        this.notificationService = notificationService;
        this.auctionService = auctionService;
    }

    private ResponseDTO<String> validateBid(BidRequestDTO bidRequestDTO){
        ResponseDTO<String> response = new ResponseDTO<>(Boolean.FALSE,"Bid could not be placed",null);
        // Introduce redis to make this API faster
        Optional<Auction> associatedAuction = auctionRepository.findAuctionByIdAndStatus(bidRequestDTO.auctionId(), AuctionStatus.LIVE.name());
        if(associatedAuction.isEmpty()){
            response.setMessage("No associated LIVE auction was found.");
            log.info("Auction not found for id: {}", bidRequestDTO.auctionId());
            response.setData(BidEventType.BID_FAILED.name());
            return response;
        }
        Auction auction = associatedAuction.get();
        if(( bidRequestDTO.amount().compareTo(auction.getBidStartPrice()) <= 0 ) ||
           (bidRequestDTO.amount().compareTo(Optional.ofNullable(auction.getHighestBid()).orElse(auction.getBidStartPrice())) <= 0 )){
            log.info("Invalid bid amount: {}", bidRequestDTO.amount());
            response.setMessage("Bid amount is not high enough");
            response.setData(BidEventType.BID_FAILED.name());
            return  response;
        }
        if((auction.getEndTime().isBefore(LocalDateTime.now()))){
            log.info("Auction expired, bid couldn't be processed: {}", bidRequestDTO.amount());
            response.setMessage("Auction expired, bid couldn't be processed");
            response.setData(BidEventType.BID_FAILED.name());
        }
        return response;
    }

    @Override
    public ResponseDTO<String> placeBid(BidRequestDTO bidRequestDTO) {
        ResponseDTO<String> response = validateBid(bidRequestDTO);
        if(!StringUtils.isEmpty(response.getData())){
            return response;
        }
        try {
            bidProducer.placeBidEvent(bidRequestDTO);
        }catch (Exception e){
            response.setMessage(e.getMessage());
            response.setData("Bid could not be processed. Please try again after sometime.");
        }
        return new ResponseDTO<>(Boolean.TRUE, "Bid accepted","Bid received and is accepted for further processing.");
    }

    private Auction auditBid(BidRequestDTO bidRequestDTO){
        // Replace with cassandra because of frequent writes
        Optional<Auction> auction = auctionRepository.findAuctionByIdAndStatus(bidRequestDTO.auctionId(), AuctionStatus.LIVE.name());
        if(auction.isPresent()){
            log.info("Saving attempted bid for auction: {}, by user: {}", bidRequestDTO.auctionId(),bidRequestDTO.user());
            Auction associatedAuction = auction.get();
            Bid attemptedBid = Bid.builder()
                    .user(bidRequestDTO.user())
                    .auction(associatedAuction)
                    .timestamp(bidRequestDTO.timestamp())
                    .buyNow(bidRequestDTO.buyNowTriggered())
                    .amount(bidRequestDTO.amount())
                    .build();
            bidRepository.save(attemptedBid);
            return associatedAuction;
        }
        return null;
    }

    @Override
    @Transactional
    public void processBid(BidRequestDTO bidRequest) {
        ResponseDTO<String> responseDTO = validateBid(bidRequest);
        Auction auction = auditBid(bidRequest);
        if(!StringUtils.isEmpty(responseDTO.getData())){
            // trigger SSE to notify the user about the failed bid
            notificationService.notifyBidFailure(bidRequest.auctionId(), bidRequest.user(),responseDTO.getMessage());
        }
        if(Objects.nonNull(auction)){
            auction.setHighestBid(bidRequest.amount());
            auction.setHighestBidder(bidRequest.user());
            if(bidRequest.buyNowTriggered().equals(Boolean.TRUE)){
                auction.setAuctionStatus(AuctionStatus.OVER);
                auction.setEndTime(LocalDateTime.now());
                auctionRepository.save(auction);
                auctionService.endAuction(auction.getProductSkuCode());
            }else{
            // update redis as well
            auctionRepository.save(auction);
            }
        }
        log.info("New bid of amount {} by {}",bidRequest.amount(), bidRequest.user());
        // trigger SSE to the client, and notify all participants
        if(bidRequest.buyNowTriggered().equals(Boolean.TRUE)){
            notificationService.notifyAuctionOver(bidRequest.auctionId(), bidRequest.user(), bidRequest.amount());
           log.info("Buy Now triggered by: {}", auction.getHighestBidder());
        }else{
            notificationService.notifyNewHighestBid(bidRequest.auctionId(), bidRequest.user(), bidRequest.amount());
            log.info("Bid accepted. New highest bid: {}", auction.getHighestBid());
        }
    }
}
