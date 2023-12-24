package com.example.reviewservice.repository;

import com.example.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Integer> {
    @Transactional(readOnly = true)
    List<Review> findByProductId(String productId);
    List<Review> deleteByProductId(String productId);
}
