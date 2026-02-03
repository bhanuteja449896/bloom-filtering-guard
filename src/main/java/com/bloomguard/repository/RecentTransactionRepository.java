package com.bloomguard.repository;

import com.bloomguard.model.entity.RecentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RecentTransactionRepository extends JpaRepository<RecentTransaction, Long> {

    Optional<RecentTransaction> findByFingerprintAndTenantId(String fingerprint, String tenantId);

    boolean existsByFingerprintAndTenantId(String fingerprint, String tenantId);

    @Modifying
    void deleteByExpiresAtBefore(Instant cutoff);

    long countByTenantId(String tenantId);
}
