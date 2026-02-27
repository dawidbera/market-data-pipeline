package com.market.pipeline.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity representing an aggregated OHLC candle in the database.
 * Used for storing and retrieving historical chart data.
 */
@Entity
@Table(name = "candles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
    private Instant windowStart;
    private Instant windowEnd;
}
