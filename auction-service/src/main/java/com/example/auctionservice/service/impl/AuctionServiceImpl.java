package com.example.auctionservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.AuctionStatus;
import com.example.auctionservice.mapper.AuctionMapper;
import com.example.auctionservice.repository.AuctionRepository;
import com.example.auctionservice.service.AuctionService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;

    @Autowired
    public AuctionServiceImpl(AuctionRepository auctionRepository, AuctionMapper auctionMapper) {
        this.auctionRepository = auctionRepository;
        this.auctionMapper = auctionMapper;
    }

    // Helper functions
    private String getStartTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now(ZoneId.of("Asia/Kolkata")).withSecond(0).withNano(0).format(formatter);
    }
    private String getEndTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now(ZoneId.of("Asia/Kolkata")).withSecond(0).withNano(0).plusSeconds(59).format(formatter);
    }

    @Override
    public ResponseDTO<String> scheduleAuction(AuctionScheduleDTO request) {
        ResponseDTO<String> response = new ResponseDTO<>(Boolean.FALSE, "Invalid request", null);
        if (request.productSkuCode().isEmpty()) {
            // Add a call to verify if a product exists
            response.setData("Associated product not found");
            log.info("Auction could not be created for id: {}", request.productSkuCode());
            return response;
        }
        Auction newAuction = auctionMapper.toEntity(request);
        Auction savedAuction = auctionRepository.save(newAuction);
        log.info("Auction: {}, scheduled from {}-{}, for product: {}", savedAuction.getId(), savedAuction.getStartTime(),
                savedAuction.getEndTime(), savedAuction.getProductSkuCode());
        return new ResponseDTO<>(
                Boolean.TRUE,
                "Request processed successfully",
                "Auction: " + savedAuction.getId() + ",scheduled for product: " + savedAuction.getProductSkuCode());
    }

    @Override
    public ResponseDTO<Auction> findAuctionBySkuCode(String skuCode) {
        ResponseDTO<Auction> responseDTO = new ResponseDTO<>(Boolean.TRUE,"Auction data found.",null);
         auctionRepository.findByProductSkuCode(skuCode).ifPresentOrElse(
                auction -> {
                    log.info("Auction associated with product: {} found.", skuCode);
                    responseDTO.setData(auction);
                },()->{
                    log.info("Associated auction not found for product: {}.",skuCode);
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("Auction data not found.");
                 }
        );
         return responseDTO;
    }

    @Override
    public ResponseDTO<String> updateStatus(Long auctionId, AuctionStatus auctionStatus) {
        ResponseDTO<String> responseDTO = new ResponseDTO<>(Boolean.FALSE,"Request failed.",null);
        Optional<Auction> auction = auctionRepository.findById(auctionId);
        auction.ifPresentOrElse(auctionData->{
            auctionData.setAuctionStatus(auctionStatus);
            auctionRepository.save(auctionData);
            responseDTO.setStatus(Boolean.TRUE);
            responseDTO.setMessage("Request processed.");
            responseDTO.setData("Status updated to " + auctionStatus);
            log.info("Updated status of auction: {}, to {}", auctionId, auctionStatus);
        },()->{
            responseDTO.setData("Auction not found.");
        });
        return responseDTO;
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void startScheduledAuction() {
        List<Auction> auctionList = auctionRepository.findByStartTimeBetweenAndActiveTrue(
                getStartTime(),
                getEndTime()
        );

        List<Long> liveAuctionList = Optional.ofNullable(auctionList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(Auction::getId)
                .toList();

        if(!CollectionUtils.isEmpty(liveAuctionList)){
            int count = auctionRepository.bulkUpdateStatus(liveAuctionList, AuctionStatus.LIVE.name());
            log.info("{} auction(s) are now LIVE", count);
        }
        log.info("No auctions(s) are scheduled to START, {}", getStartTime());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void endScheduledAuction() {
        List<Auction> auctionList = auctionRepository.findByEndTimeBetweenAndActiveTrue(
                getStartTime(),
                getEndTime()
        );

        List<Long> liveAuctionList = Optional.ofNullable(auctionList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(Auction::getId)
                .toList();

        if(!CollectionUtils.isEmpty(liveAuctionList)){
            int count = auctionRepository.bulkUpdateStatus(liveAuctionList, AuctionStatus.OVER.name());
            log.info("{} auction(s) have now ENDED", count);
        }
        log.info("No auctions(s) are scheduled to END, {}", getStartTime());
    }
}
