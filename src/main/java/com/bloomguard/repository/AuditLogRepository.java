package com.bloomguard.repository;

import com.bloomguard.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTenantId(String tenantId, Pageable pageable);

    Page<AuditLog> findByFilterNameAndTenantId(String filterName, String tenantId, Pageable pageable);

    List<AuditLog> findByFilterNameAndCreatedAtBetween(String filterName, Instant start, Instant end);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.filterName = :filterName AND a.result = :result")
    long countByFilterNameAndResult(@Param("filterName") String filterName, @Param("result") String result);

    @Query("SELECT AVG(a.latencyMicros) FROM AuditLog a WHERE a.filterName = :filterName AND a.createdAt > :since")
    Double getAverageLatency(@Param("filterName") String filterName, @Param("since") Instant since);

    void deleteByCreatedAtBefore(Instant cutoff);
}
