package com.bloomguard.controller;

import com.bloomguard.model.dto.request.CreateFilterRequest;
import com.bloomguard.model.dto.response.ApiResponse;
import com.bloomguard.model.dto.response.BloomFilterStats;
import com.bloomguard.model.dto.response.FilterListResponse;
import com.bloomguard.model.entity.FilterConfiguration;
import com.bloomguard.repository.FilterConfigRepository;
import com.bloomguard.security.TenantContext;
import com.bloomguard.service.BackupRecoveryService;
import com.bloomguard.service.BloomFilterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bloom")
public class AdminController {

    private final BloomFilterService bloomFilterService;
    private final BackupRecoveryService backupRecoveryService;
    private final FilterConfigRepository filterConfigRepository;

    @Autowired
    public AdminController(BloomFilterService bloomFilterService,
                          BackupRecoveryService backupRecoveryService,
                          FilterConfigRepository filterConfigRepository) {
        this.bloomFilterService = bloomFilterService;
        this.backupRecoveryService = backupRecoveryService;
        this.filterConfigRepository = filterConfigRepository;
    }

    @GetMapping("/filters")
    public ResponseEntity<ApiResponse<FilterListResponse>> listFilters() {
        String tenantId = TenantContext.getCurrentTenantId();
        List<FilterConfiguration> configs = filterConfigRepository.findByTenantIdAndActiveTrue(tenantId);
        
        List<FilterListResponse.FilterInfo> filterInfos = configs.stream()
                .map(config -> new FilterListResponse.FilterInfo(
                        config.getFilterName(),
                        config.getExpectedInsertions(),
                        config.getFalsePositiveRate(),
                        config.isRotatable(),
                        config.isActive()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(new FilterListResponse(filterInfos)));
    }

    @PostMapping("/filters")
    public ResponseEntity<ApiResponse<BloomFilterStats>> createFilter(
            @Valid @RequestBody CreateFilterRequest request) {
        String tenantId = TenantContext.getCurrentTenantId();
        
        FilterConfiguration config = new FilterConfiguration();
        config.setFilterName(request.getFilterName());
        config.setExpectedInsertions(request.getExpectedInsertions());
        config.setFalsePositiveRate(request.getFalsePositiveRate());
        config.setRotatable(request.isRotatable());
        config.setRotationDays(request.getRotationDays());
        config.setTenantId(tenantId);
        config.setActive(true);
        
        filterConfigRepository.save(config);
        
        bloomFilterService.createFilter(
                request.getFilterName(),
                request.getExpectedInsertions(),
                request.getFalsePositiveRate());
        
        BloomFilterStats stats = bloomFilterService.getStats(request.getFilterName());
        return ResponseEntity.ok(ApiResponse.success(stats, "Filter created successfully"));
    }

    @GetMapping("/stats/{filterName}")
    public ResponseEntity<ApiResponse<BloomFilterStats>> getStats(@PathVariable String filterName) {
        BloomFilterStats stats = bloomFilterService.getStats(filterName);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/clear/{filterName}")
    public ResponseEntity<ApiResponse<Void>> clearFilter(@PathVariable String filterName) {
        bloomFilterService.clear(filterName);
        return ResponseEntity.ok(ApiResponse.success(null, "Filter cleared successfully"));
    }

    @PostMapping("/rebuild/{filterName}")
    public ResponseEntity<ApiResponse<Long>> rebuildFilter(@PathVariable String filterName) {
        long count = backupRecoveryService.rebuildFromBackup(filterName);
        return ResponseEntity.ok(ApiResponse.success(count, "Rebuilt filter with " + count + " items"));
    }
}
