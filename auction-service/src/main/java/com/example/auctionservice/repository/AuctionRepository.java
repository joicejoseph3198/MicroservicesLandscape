package com.example.auctionservice.repository;

import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction,Long> {
    Optional<Auction> findByProductSkuCode(String skuCode);
    @Query(nativeQuery = true,
            value = "SELECT * FROM auction" +
                    " WHERE id =: id " +
                    " AND status =: status" +
                    " AND active = true")
    Optional<Auction> findAuctionByIdAndStatus(Long id, Status status);
}
