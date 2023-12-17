package com.example.reviewservice;

import com.example.reviewservice.entity.Review;
import com.example.reviewservice.enums.Rating;
import com.example.reviewservice.repository.ReviewRepository;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceTests extends MySqlTestBase{
    @Autowired
    private ReviewRepository reviewRepository;
    private static Review savedReview;

    @BeforeEach
    void setUpDb(){
        reviewRepository.deleteAll();
        Review review = Review
                .builder()
                .description("Grape!")
                .productId("TestId")
                .title("Testing Review")
                .rating(Rating.FOUR)
                .build();

        savedReview = reviewRepository.save(review);
        assertEquals(savedReview,review);
    }

    @Test
    void create(){
        Review newReview = Review
                .builder()
                .rating(Rating.THREE)
                .productId("TestId")
                .title("Mid Product")
                .description("Mid.")
                .build();
        reviewRepository.save(newReview);
        assertEquals(2, reviewRepository.findByProductId("TestId").size());
    }

    @Test
    void delete() {
        reviewRepository.delete(savedReview);
        assertFalse(reviewRepository.existsById(savedReview.getId()));
    }

    @Test
    void update() {
        savedReview.setTitle("Updated Review");
        reviewRepository.save(savedReview);

        Review foundReview = reviewRepository.findById(savedReview.getId()).get();
        assertEquals("Updated Review", foundReview.getTitle());
    }

    @Test
    void getByProductId() {
        List<Review> foundReviews = reviewRepository.findByProductId(savedReview.getProductId());
        assertEquals(savedReview, foundReviews.get(0));
    }
}
