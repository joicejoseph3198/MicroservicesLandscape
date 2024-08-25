package com.example.auctionservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.Status;
import com.example.auctionservice.mapper.AuctionMapper;
import com.example.auctionservice.repository.AuctionRepository;
import com.example.auctionservice.service.AuctionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public ResponseDTO<String> updateStatus(Long auctionId, Status status) {
        ResponseDTO<String> responseDTO = new ResponseDTO<>(Boolean.FALSE,"Request failed.",null);
        Optional<Auction> auction = auctionRepository.findById(auctionId);
        auction.ifPresentOrElse(auctionData->{
            auctionData.setStatus(status);
            auctionRepository.save(auctionData);
            responseDTO.setStatus(Boolean.TRUE);
            responseDTO.setMessage("Request processed.");
            responseDTO.setData("Status updated to "+ status);
            log.info("Updated status of auction: {}, to {}", auctionId,status);
        },()->{
            responseDTO.setData("Auction not found.");
        });
        return responseDTO;
    }
}
