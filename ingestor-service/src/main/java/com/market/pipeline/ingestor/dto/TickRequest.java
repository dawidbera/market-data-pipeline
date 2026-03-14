package com.market.pipeline.ingestor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for market tick requests.
 * Used for receiving market data from external APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickRequest {
    private String symbol;
    private Double price;
    private Long volume;
    private Long timestamp;
}
