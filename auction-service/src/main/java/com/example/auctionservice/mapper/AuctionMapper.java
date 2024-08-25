package com.example.auctionservice.mapper;

import com.example.UtilService.base.BaseMapper;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.entity.Auction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface AuctionMapper extends BaseMapper<Auction, AuctionScheduleDTO> {
    AuctionMapper INSTANCE = Mappers.getMapper(AuctionMapper.class);
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    @Override
    Auction toEntity(AuctionScheduleDTO dto);

    default ZonedDateTime mapStringToZonedDateTime(String dateTimeString) {
        // Define the formatter to match the input string pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        // Parse the string into a LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
        // Convert the LocalDateTime to ZonedDateTime using the system default timezone
        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
    }
}