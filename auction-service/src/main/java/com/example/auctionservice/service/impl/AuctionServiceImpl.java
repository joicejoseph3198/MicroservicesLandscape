package com.example.auctionservice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.example.auctionservice.messaging.AuctionProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.AuctionDetailsDTO;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.AuctionStatus;
import com.example.auctionservice.enums.Status;
import com.example.auctionservice.feign.ProductClient;
import com.example.auctionservice.mapper.AuctionDetailsMapper;
import com.example.auctionservice.mapper.AuctionMapper;
import com.example.auctionservice.repository.AuctionRepository;
import com.example.auctionservice.service.AuctionService;
import com.example.auctionservice.service.SseService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final AuctionDetailsMapper auctionDetailsMapper;
    private final ProductClient productClient;
    private final SseService notificationService;
    private final AuctionProducer auctionProducer;

    @Autowired
    public AuctionServiceImpl(AuctionRepository auctionRepository, AuctionMapper auctionMapper, AuctionDetailsMapper auctionDetailsMapper, ProductClient productClient, SseService sseService, AuctionProducer auctionProducer) {
        this.auctionRepository = auctionRepository;
        this.auctionMapper = auctionMapper;
        this.auctionDetailsMapper = auctionDetailsMapper;
        this.productClient = productClient;
        this.notificationService = sseService;
        this.auctionProducer = auctionProducer;
    }

    /**Helper functions**/
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
            response.setData("Associated product not found");
            log.info("Auction could not be created for id: {}", request.productSkuCode());
            return response;
        }
        Auction newAuction = auctionMapper.toEntity(request);
        try{
            productClient.updateProductStatus(request.productSkuCode(), Status.SCHEDULED);
            Auction savedAuction = auctionRepository.save(newAuction);
            log.info("Auction: {}, scheduled from {}-{}, for product: {}", savedAuction.getId(), savedAuction.getStartTime(),
                    savedAuction.getEndTime(), savedAuction.getProductSkuCode().toUpperCase());
            return new ResponseDTO<>(
                    Boolean.TRUE,
                    "Request processed successfully",
                    "Auction: " + savedAuction.getId() + ", scheduled for product: " + savedAuction.getProductSkuCode().toUpperCase());
        }catch(Exception e){
            log.error("Ran into an exception while scheduling auction for product {}", request.productSkuCode().toUpperCase());
            log.error("Error: {}", e.getMessage());
            response.setData("Ran into an exception");
            return response;
        }
    }

    @Override
    public ResponseDTO<String> endAuction(String skuCode) {
        int count = auctionRepository.setDeletedTrueBySkuCode(skuCode);
        productClient.updateProductStatus(skuCode, Status.SOLD);
        log.info("{} record(s) were deleted", count);
        if(count  >= 1)
            return new ResponseDTO<>(Boolean.TRUE, "Record(s) deleted successfully.","Count: "+count);
        else
            return new ResponseDTO<>(Boolean.FALSE, "Record deletion failed.",null);
    }

    @Override
    public ResponseDTO<AuctionDetailsDTO> findAuctionBySkuCode(String skuCode) {
        ResponseDTO<AuctionDetailsDTO> responseDTO = new ResponseDTO<>(Boolean.TRUE,"Auction data found.",null);
         auctionRepository.findByProductSkuCodeAndAuctionStatusIn(
                 skuCode,
                 List.of(AuctionStatus.LIVE.name(), AuctionStatus.SCHEDULED.name(), AuctionStatus.OVER.name())
         ).ifPresentOrElse(
                 auction -> {
                     log.info("Auction associated with product: {} found.", skuCode);
                     AuctionDetailsDTO auctionDetailsDTO = auctionDetailsMapper.toDto(auction);
                     responseDTO.setData(auctionDetailsDTO);
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
            auctionProducer.produceAuctionPublishEvent(auctionData);
            responseDTO.setStatus(Boolean.TRUE);
            responseDTO.setMessage("Request processed.");
            responseDTO.setData("Status updated to " + auctionStatus);
            log.info("Updated status of auction: {}, to {}", auctionId, auctionStatus);
        },()->responseDTO.setData("Auction not found."));
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
            auctionList
                    .forEach(auction -> {
                        productClient.updateProductStatus(auction.getProductSkuCode(), Status.LIVE);
                        notificationService.notifyAuctionStart(auction.getId());
                        auctionProducer.produceAuctionPublishEvent(auction);
                    });
            int count = auctionRepository.bulkUpdateStatus(liveAuctionList, AuctionStatus.LIVE);
            log.info("{} auction(s) are now LIVE", count);
        }
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
            auctionList
                    .forEach(auction -> {
                        productClient.updateProductStatus(auction.getProductSkuCode(), Status.SOLD);
                        notificationService.notifyAuctionOver(auction.getId(), auction.getHighestBidder(), auction.getHighestBid());
                        auctionProducer.produceAuctionUnPublishEvent(auction.getId());
                    });
            int count = auctionRepository.bulkUpdateStatus(liveAuctionList, AuctionStatus.OVER);
            log.info("{} auction(s) have now ENDED", count);
        }
    }

    @Override
    @Transactional
    public void purgeStaleAuction() {
        List<Auction> staleAuctions = auctionRepository.findByEndTimeBeforeAndActiveTrue(LocalDateTime.now());

        List<Long> staleAuctionIds = Optional.ofNullable(staleAuctions)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(Auction::getId)
                .toList();

        if(!CollectionUtils.isEmpty(staleAuctionIds)){
            int count = auctionRepository.bulkUpdateStatus(staleAuctionIds, AuctionStatus.OVER);
            staleAuctionIds.forEach(auctionProducer::produceAuctionUnPublishEvent);
            log.info("{} auction(s) have been purged", count);
        }
    }
}
