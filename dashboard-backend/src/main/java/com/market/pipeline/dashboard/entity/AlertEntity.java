package com.market.pipeline.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity representing a market anomaly alert in the database.
 * Captures information about price spikes or unusual volume events.
 */
@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private String type;
    @Column(columnDefinition = "TEXT")
    private String message;
    private Instant timestamp;
}
