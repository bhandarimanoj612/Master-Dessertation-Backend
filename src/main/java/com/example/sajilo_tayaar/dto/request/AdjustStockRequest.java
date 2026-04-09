package com.example.sajilo_tayaar.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdjustStockRequest {
    private Long shopId;
    private Integer delta; // +10 or -3
}