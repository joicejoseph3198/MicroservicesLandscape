package com.example.auctionservice.entity;

import com.example.auctionservice.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private Status status = Status.SCHEDULED;
    @Version
    private Long version;
}
