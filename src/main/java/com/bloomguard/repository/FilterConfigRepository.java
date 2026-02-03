package com.bloomguard.repository;

import com.bloomguard.model.entity.FilterConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterConfigRepository extends JpaRepository<FilterConfiguration, Long> {

    Optional<FilterConfiguration> findByFilterNameAndTenantId(String filterName, String tenantId);

    Optional<FilterConfiguration> findByFilterName(String filterName);

    List<FilterConfiguration> findByTenantIdAndActiveTrue(String tenantId);

    List<FilterConfiguration> findByRotatableTrue();

    boolean existsByFilterNameAndTenantId(String filterName, String tenantId);
}
