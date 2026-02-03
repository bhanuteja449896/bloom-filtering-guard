package com.bloomguard.repository;

import com.bloomguard.model.entity.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {

    Optional<StolenCard> findByCardHashAndTenantId(String cardHash, String tenantId);

    boolean existsByCardHashAndTenantIdAndActiveTrue(String cardHash, String tenantId);

    long countByTenantIdAndActiveTrue(String tenantId);
}
