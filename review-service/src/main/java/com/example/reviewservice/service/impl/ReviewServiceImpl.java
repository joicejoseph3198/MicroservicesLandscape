package com.example.reviewservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.reviewservice.dto.ReviewDTO;
import com.example.reviewservice.entity.Review;
import com.example.reviewservice.enums.Rating;
import com.example.reviewservice.mapper.ReviewMapper;
import com.example.reviewservice.repository.ReviewRepository;
import com.example.reviewservice.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final Faker faker;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, ReviewMapper reviewMapper){
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.faker = new Faker();
    }
    @Override
    public ResponseDTO<String> createReview(ReviewDTO reviewDTO) {
        ResponseDTO<String> responseDTO =
                new ResponseDTO<>(Boolean.TRUE,"Request processed successfully.",null);
        Optional.ofNullable(reviewDTO)
                .map(reviewMapper::toEntity)
                .ifPresentOrElse(entity-> {
                    LOG.debug("Adding review to product ({}).",reviewDTO.productId());
                    reviewRepository.save(entity);
                    responseDTO.setData("Added review for product ("+reviewDTO.productId()+").");
                },()->{
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("Couldn't add review");
                });
        return responseDTO;
    }

    @Override
    public ResponseDTO<List<ReviewDTO>> getReviewByProductId(String productId) {
        ResponseDTO<List<ReviewDTO>> responseDTO =
                new ResponseDTO<>(Boolean.TRUE,"Request processed successfully.",null);

        Optional.ofNullable(reviewRepository.findByProductId(productId))
                .ifPresentOrElse(reviews-> {
                    List<ReviewDTO> reviewDTOList = reviewMapper.toDtoList(reviews);
                    responseDTO.setData(reviewDTOList);
                }, ()->{
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("Reviews not found for the associated product.");
                });
        return responseDTO;
    }

    @Override
    public ResponseDTO<String> insertDummyData() {
        ResponseDTO<String> responseDTO = new ResponseDTO<>(Boolean.TRUE,"Data insertion complete.",null);
        generateDummyReviews();
        return responseDTO;
    }

    @Transactional
    private void generateDummyReviews(){
        if(reviewRepository.findAll().isEmpty()){
            List<Review> reviewList = IntStream.rangeClosed(1,300)
                    .mapToObj(i->
                        new Review(
                                i,
                                String.valueOf(faker.random().nextInt(1,100)),
                                faker.lorem().sentence(),
                                faker.lorem().sentence(),
                                faker.options().option(Rating.class)
                        ))
                    .toList();
            reviewRepository.saveAll(reviewList);
        }
    }

}
