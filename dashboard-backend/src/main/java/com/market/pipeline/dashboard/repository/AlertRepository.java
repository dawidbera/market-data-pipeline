package com.market.pipeline.dashboard.repository;

import com.market.pipeline.dashboard.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing and persisting {@link AlertEntity} data.
 */
@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
    /**
     * Finds historical alerts for a specific market symbol, 
     * ordered by their timestamp in descending order.
     * 
     * @param symbol the market symbol to search for
     * @return a list of alerts for the symbol, newest first
     */
    List<AlertEntity> findBySymbolOrderByTimestampDesc(String symbol);
}
