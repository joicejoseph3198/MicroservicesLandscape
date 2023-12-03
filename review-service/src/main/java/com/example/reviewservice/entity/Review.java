package com.example.reviewservice.entity;

import com.example.reviewservice.enums.Rating;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "reviews",indexes = {@Index(name = "review_idx",
        unique = true,columnList = "id,productId")})
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue
    private Integer id;
    private String productId;
    private String content;
    private String title;
    private Rating rating;

}
