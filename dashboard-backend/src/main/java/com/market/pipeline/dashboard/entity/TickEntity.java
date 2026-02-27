package com.market.pipeline.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity representing a single market tick (price update) in the database.
 * Stored in a TimescaleDB hypertable for efficient time-series analysis.
 */
@Entity
@Table(name = "ticks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private Double price;
    private Long volume;
    private Instant timestamp;
}
