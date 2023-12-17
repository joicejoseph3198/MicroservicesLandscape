package com.example.reviewservice.entity;

import com.example.reviewservice.enums.Rating;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String productId;
    private String description;
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(name = "rating")
    private Rating rating;

}
