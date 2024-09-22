package com.example.auctionservice.repository;

import com.example.auctionservice.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction,Long> {
    Optional<Auction> findByProductSkuCode(String skuCode);

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM auction " +
                    "WHERE id =?1 " +
                    "AND status =?2 " +
                    "AND active = true")
    Optional<Auction> findAuctionByIdAndStatus(Long id, String status);

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM auction WHERE start_time BETWEEN ?1 AND ?2 AND active = true"
     )
    List<Auction> findByStartTimeBetweenAndActiveTrue(String startDateTime, String endDateTime);

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM auction WHERE end_time BETWEEN ?1 AND ?2 AND active = true"
    )
    List<Auction> findByEndTimeBetweenAndActiveTrue(String startDateTime, String endDateTime);


    @Modifying
    @Query(
            nativeQuery = true,
            value = "UPDATE auction " +
                    "SET auction_status = ?2 and deleted = true" +
                    "WHERE id IN (?1) AND active = true"

    )
    int bulkUpdateStatus(List<Long> ids, String status);

    @Modifying
    @Query(
            nativeQuery = true,
            value = "UPDATE auction " +
                    "SET deleted = true and active = false and auction_status = OVER" +
                    "WHERE id IN (?1) AND active = true"

    )
    int setDeletedTrueById(Long id);
}
