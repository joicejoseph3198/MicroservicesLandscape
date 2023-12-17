package com.example.reviewservice.mapper;

import com.example.UtilService.base.BaseMapper;
import com.example.reviewservice.dto.ReviewDTO;
import com.example.reviewservice.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper extends BaseMapper<Review, ReviewDTO> {
    ReviewMapper INSTANCE = Mappers.getMapper(ReviewMapper.class);
    Review toEntity(ReviewDTO reviewDTO);
    ReviewDTO toDto(Review review);
    List<Review> toEntityList(Iterable<ReviewDTO> reviewDTOS);
    List<ReviewDTO> toDtoList(Iterable<Review> reviews);
}
