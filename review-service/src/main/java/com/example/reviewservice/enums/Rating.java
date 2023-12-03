package com.example.reviewservice.enums;

public enum Rating {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);
    private Integer value;

    private Rating(Integer value){
        this.value = value;
    }
}
