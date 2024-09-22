package com.example.auctionservice.mapper;

import com.example.UtilService.base.BaseMapper;
import com.example.auctionservice.dto.AuctionDetailsDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.AuctionStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AuctionDetailsMapper extends BaseMapper<Auction, AuctionDetailsDTO> {
    AuctionMapper INSTANCE = Mappers.getMapper(AuctionMapper.class);

    @Mapping(source = "auctionStatus", target = "auctionStatus", qualifiedByName = "statusToLabel")
    @Override
    AuctionDetailsDTO toDto(Auction entity);

    @Named("statusToLabel")
    default String statusToLabel(AuctionStatus status) {
        return status.label;
    }
}
