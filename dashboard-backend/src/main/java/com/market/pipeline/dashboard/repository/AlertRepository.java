package com.market.pipeline.dashboard.repository;

import com.market.pipeline.dashboard.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing and persisting {@link AlertEntity} data.
 */
@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
}
