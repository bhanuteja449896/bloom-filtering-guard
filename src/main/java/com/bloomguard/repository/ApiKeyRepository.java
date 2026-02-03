package com.bloomguard.repository;

import com.bloomguard.model.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);

    boolean existsByKeyHash(String keyHash);

    @Modifying
    @Query("UPDATE ApiKey a SET a.lastUsedAt = :timestamp WHERE a.keyHash = :keyHash")
    void updateLastUsedAt(@Param("keyHash") String keyHash, @Param("timestamp") Instant timestamp);

    long countByTenantIdAndActiveTrue(String tenantId);
}
