package com.example.auctionservice.service.impl;

import com.example.UtilService.base.Event;
import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.BidRequestDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.entity.Bid;
import com.example.auctionservice.enums.Status;
import com.example.auctionservice.messaging.BidProducer;
import com.example.auctionservice.repository.AuctionRepository;
import com.example.auctionservice.repository.BidRepository;
import com.example.auctionservice.service.BidService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;

    private final AuctionRepository auctionRepository;
    private final BidProducer bidProducer;

    @Autowired
    public BidServiceImpl(AuctionRepository auctionRepository, StreamBridge streamBridge, BidProducer bidProducer,
                          BidRepository bidRepository) {
        this.auctionRepository = auctionRepository;
        this.bidProducer = bidProducer;
        this.bidRepository = bidRepository;
    }

    private ResponseDTO<String> validateBid(BidRequestDTO bidRequestDTO){
        ResponseDTO<String> response = new ResponseDTO<>(Boolean.FALSE,"Bid could not be placed",null);
        // Introduce redis to make this API faster
        Optional<Auction> associatedAuction = auctionRepository.findAuctionByIdAndStatus(bidRequestDTO.auctionId(), Status.LIVE);
        if(associatedAuction.isEmpty()){
            response.setData("No associated LIVE auction was found.");
            log.info("Auction not found for id: {}", bidRequestDTO.auctionId());
            return response;
        }
        Auction auction = associatedAuction.get();
        if(( bidRequestDTO.amount().compareTo(auction.getBidStartPrice()) <= 0 ) ||
           ( bidRequestDTO.amount().compareTo(auction.getHighestBid()) <= 0 )){
            log.info("Invalid bid amount: {}", bidRequestDTO.amount());
            response.setData("Bid amount is not high enough");
            return  response;
        }
        return response;
    }

    @Override
    public ResponseDTO<String> placeBid(BidRequestDTO bidRequestDTO) {
        ResponseDTO<String> response = validateBid(bidRequestDTO);
        if(!response.getData().isEmpty()){
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
        // Replace with redis
        Optional<Auction> auction = auctionRepository.findAuctionByIdAndStatus(bidRequestDTO.auctionId(), Status.LIVE);
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
    public ResponseDTO<String> processBid(BidRequestDTO bidRequest) {
        ResponseDTO<String> responseDTO = validateBid(bidRequest);
        Auction auction = auditBid(bidRequest);
        if(!responseDTO.getData().isEmpty()){
            return responseDTO;
        }
        if(Objects.nonNull(auction)){
            auction.setHighestBid(bidRequest.amount());
            // update redis as well
            auctionRepository.save(auction);
        }
        // trigger SSE to the client, and notify all participants

        return null;
    }

}
