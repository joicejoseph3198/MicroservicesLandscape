package com.example.auctionservice.repository;

import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction,Long> {
    @Query(
            nativeQuery = true,
            value = "SELECT * FROM auction " +
                    "WHERE product_sku_code = ?1 " +
                    "AND auction_status IN (?2) " +
                    "ORDER BY last_modified_date DESC " +
                    "LIMIT 1")
    Optional<Auction> findByProductSkuCodeAndAuctionStatusIn(String skuCode, List<String> statuses);

    @Query(
            nativeQuery = true,
            value = "SELECT * FROM auction " +
                    "WHERE id =?1 " +
                    "AND auction_status = ?2 " +
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
                    "SET auction_status = ?2 " +
                    "WHERE id IN (?1) AND active = true"

    )
    int bulkUpdateStatus(List<Long> ids, AuctionStatus status);

    @Modifying
    @Query(
            nativeQuery = true,
            value = "UPDATE auction " +
                    "SET deleted = true, active = false, auction_status = 'OVER' " +
                    "WHERE product_sku_code = (?1) AND (auction_status = 'LIVE' OR auction_status = 'SCHEDULED')"
    )
    int setDeletedTrueBySkuCode(String skuCode);

    List<Auction> findByEndTimeBeforeAndActiveTrue(LocalDateTime localDateTime);
}
