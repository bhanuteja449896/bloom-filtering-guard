package com.bloomguard.repository;

import com.bloomguard.model.entity.BloomFilterBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface BackupRepository extends JpaRepository<BloomFilterBackup, Long> {

    List<BloomFilterBackup> findByFilterNameAndTenantId(String filterName, String tenantId);

    @Query("SELECT b FROM BloomFilterBackup b WHERE b.filterName = :filterName AND b.tenantId = :tenantId")
    Stream<BloomFilterBackup> streamByFilterNameAndTenantId(@Param("filterName") String filterName, 
                                                            @Param("tenantId") String tenantId);

    boolean existsByFilterNameAndItemHashAndTenantId(String filterName, String itemHash, String tenantId);

    @Modifying
    @Query("DELETE FROM BloomFilterBackup b WHERE b.filterName = :filterName AND b.tenantId = :tenantId")
    void deleteByFilterNameAndTenantId(@Param("filterName") String filterName, @Param("tenantId") String tenantId);

    @Modifying
    void deleteByExpiresAtBefore(Instant cutoff);

    long countByFilterNameAndTenantId(String filterName, String tenantId);
}
