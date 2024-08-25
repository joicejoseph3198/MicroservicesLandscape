package com.example.auctionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;
    @Column(nullable = false)
    private String user;
    @Column(scale = 2)
    private BigDecimal amount;
    private Boolean buyNow = Boolean.FALSE;
    private ZonedDateTime timestamp;
}
