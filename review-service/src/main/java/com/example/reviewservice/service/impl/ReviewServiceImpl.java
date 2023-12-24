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
import org.springframework.util.CollectionUtils;

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
                    LOG.debug("Failed to add review to product ({}).",reviewDTO.productId());
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
                    LOG.debug("Found associated review(s) (Count: {}).",reviews.size());
                    List<ReviewDTO> reviewDTOList = reviewMapper.toDtoList(reviews);
                    responseDTO.setData(reviewDTOList);
                }, ()->{
                    LOG.debug("Associated review(s) not found (ProductId: {}).",productId);
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("Reviews not found for the associated product.");
                });
        return responseDTO;
    }

    @Override
    @Transactional
    public ResponseDTO<String> deleteReview(Integer reviewId){
        ResponseDTO<String> responseDTO = new ResponseDTO<>(
                Boolean.TRUE,
                "Request processed successfully.",
                "Review "+reviewId+" deleted.");

        reviewRepository.findById(reviewId)
                .ifPresentOrElse(
                        review -> {
                            LOG.debug("Deleting review (Id: {}).",reviewId);
                            reviewRepository.delete(review);
                            },
                        ()->{
                            LOG.debug("Failed to delete review (Id: {}).",reviewId);
                            responseDTO.setStatus(Boolean.FALSE);
                            responseDTO.setMessage("Request Failed");
                            responseDTO.setData("Associated review couldn't be found.");
                        });
        return responseDTO;
    }
    @Override
    @Transactional
    public ResponseDTO<List<Integer>> deleteAssociatedReview(String productId){
        ResponseDTO<List<Integer>> responseDTO = new ResponseDTO<>(
                Boolean.TRUE,
                "Request processed successfully.",
                null);

        List<Review> deletedReviews = reviewRepository.deleteByProductId(productId);
        Optional.ofNullable(deletedReviews)
                .filter(List::isEmpty)
                .ifPresentOrElse(
                        emptyList -> {
                            LOG.debug("Associated review(s) not found (ProductId: {}).",productId);
                            responseDTO.setStatus(Boolean.FALSE);
                            responseDTO.setMessage("Couldn't find associated reviews");
                            },
                        ()-> {
                            LOG.debug("Deleting associated review(s) (ProductId: {}, Count: {}).",productId,deletedReviews.size());
                            responseDTO.setData(deletedReviews.stream().map(Review::getId).toList());
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
            LOG.debug("Creating review(s) (Count: {}).",reviewList.size());
            reviewRepository.saveAll(reviewList);
        }
    }
}
