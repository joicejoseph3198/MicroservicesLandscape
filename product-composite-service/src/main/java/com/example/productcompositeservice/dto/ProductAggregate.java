package com.example.productcompositeservice.dto;


import java.util.List;
public record ProductAggregate(ConfigureProductDTO productDTO, List<ReviewDTO> reviewDTOList, AuctionDetailsDTO auctionDetailsDTO) {
}
