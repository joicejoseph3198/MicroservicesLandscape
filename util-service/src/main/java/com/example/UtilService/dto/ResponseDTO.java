package com.example.UtilService.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO<T> {
    private Boolean status;
    private String message;
    private T data;
}
