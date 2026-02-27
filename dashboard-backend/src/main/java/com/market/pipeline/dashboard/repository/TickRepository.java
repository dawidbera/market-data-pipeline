package com.market.pipeline.dashboard.repository;

import com.market.pipeline.dashboard.entity.TickEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing and persisting {@link TickEntity} data.
 */
@Repository
public interface TickRepository extends JpaRepository<TickEntity, Long> {
}
