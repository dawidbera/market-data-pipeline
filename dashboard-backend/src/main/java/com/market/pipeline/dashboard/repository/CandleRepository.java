package com.market.pipeline.dashboard.repository;

import com.market.pipeline.dashboard.entity.CandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing and persisting {@link CandleEntity} data.
 * Provides custom methods for retrieving historical data by symbol.
 */
@Repository
public interface CandleRepository extends JpaRepository<CandleEntity, Long> {
    /**
     * Finds historical candle records for a specific market symbol, 
     * ordered by their window start time in descending order.
     * 
     * @param symbol the market symbol to search for
     * @return a list of candles for the symbol, newest first
     */
    List<CandleEntity> findBySymbolOrderByWindowStartDesc(String symbol);
}
