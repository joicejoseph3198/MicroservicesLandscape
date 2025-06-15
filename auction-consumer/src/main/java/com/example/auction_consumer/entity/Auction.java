package com.example.auction_consumer.entity;

import com.example.auction_consumer.enums.AuctionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
public class Auction extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String productSkuCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @OneToMany(mappedBy = "auction")
    private List<Bid> bids;
    @Column(scale = 2)
    private BigDecimal highestBid;
    private String highestBidder;
    @Column(scale = 2)
    private BigDecimal bidStartPrice;
    @Column(scale = 2)
    private BigDecimal buyNowPrice;
    @Enumerated(EnumType.STRING)
    private AuctionStatus auctionStatus = AuctionStatus.SCHEDULED;
    @Version
    private Long version;
}
