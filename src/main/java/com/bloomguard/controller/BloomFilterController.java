package com.bloomguard.controller;

import com.bloomguard.model.dto.request.*;
import com.bloomguard.model.dto.response.*;
import com.bloomguard.service.AuditService;
import com.bloomguard.service.BackupRecoveryService;
import com.bloomguard.service.BloomFilterService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bloom")
public class BloomFilterController {

    private final BloomFilterService bloomFilterService;
    private final AuditService auditService;
    private final BackupRecoveryService backupRecoveryService;

    @Value("${bloomguard.backup.enabled:true}")
    private boolean backupEnabled;

    @Autowired
    public BloomFilterController(BloomFilterService bloomFilterService,
                                AuditService auditService,
                                BackupRecoveryService backupRecoveryService) {
        this.bloomFilterService = bloomFilterService;
        this.auditService = auditService;
        this.backupRecoveryService = backupRecoveryService;
    }

    @PostMapping("/check")
    @Timed(value = "api.bloom.check", description = "Check endpoint timing")
    public ResponseEntity<ApiResponse<CheckResponse>> check(@Valid @RequestBody CheckRequest request) {
        long startTime = System.nanoTime();
        
        boolean result = bloomFilterService.mightContain(request.getFilterName(), request.getItem());
        
        long latencyMicros = (System.nanoTime() - startTime) / 1000;
        
        auditService.logCheck(request.getFilterName(), request.getItem(), result, latencyMicros);
        
        CheckResponse response = new CheckResponse(request.getFilterName(), result, latencyMicros);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/add")
    @Timed(value = "api.bloom.add", description = "Add endpoint timing")
    public ResponseEntity<ApiResponse<AddResponse>> add(@Valid @RequestBody AddRequest request) {
        long startTime = System.nanoTime();
        
        boolean added = bloomFilterService.add(request.getFilterName(), request.getItem());
        
        long latencyMicros = (System.nanoTime() - startTime) / 1000;
        
        auditService.logAdd(request.getFilterName(), request.getItem(), added, latencyMicros);
        
        if (backupEnabled && added) {
            backupRecoveryService.backupItem(request.getFilterName(), request.getItem(), null);
        }
        
        AddResponse response = new AddResponse(request.getFilterName(), added, latencyMicros);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/check-and-add")
    @Timed(value = "api.bloom.check_and_add", description = "Check and add endpoint timing")
    public ResponseEntity<ApiResponse<CheckAndAddResponse>> checkAndAdd(@Valid @RequestBody AddRequest request) {
        long startTime = System.nanoTime();
        
        BloomFilterService.CheckAndAddResult result = bloomFilterService.checkAndAdd(
                request.getFilterName(), request.getItem());
        
        long latencyMicros = (System.nanoTime() - startTime) / 1000;
        
        if (!result.existedBefore()) {
            auditService.logAdd(request.getFilterName(), request.getItem(), result.addedNow(), latencyMicros);
            if (backupEnabled && result.addedNow()) {
                backupRecoveryService.backupItem(request.getFilterName(), request.getItem(), null);
            }
        } else {
            auditService.logCheck(request.getFilterName(), request.getItem(), true, latencyMicros);
        }
        
        CheckAndAddResponse response = new CheckAndAddResponse(
                request.getFilterName(), result.existedBefore(), result.addedNow(), latencyMicros);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/batch/check")
    @Timed(value = "api.bloom.batch_check", description = "Batch check endpoint timing")
    public ResponseEntity<ApiResponse<BatchCheckResponse>> batchCheck(@Valid @RequestBody BatchCheckRequest request) {
        long startTime = System.nanoTime();
        
        Map<String, Boolean> results = bloomFilterService.mightContainBatch(
                request.getFilterName(), request.getItems());
        
        long latencyMicros = (System.nanoTime() - startTime) / 1000;
        
        BatchCheckResponse response = new BatchCheckResponse(request.getFilterName(), results, latencyMicros);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/batch/add")
    @Timed(value = "api.bloom.batch_add", description = "Batch add endpoint timing")
    public ResponseEntity<ApiResponse<BatchAddResponse>> batchAdd(@Valid @RequestBody BatchAddRequest request) {
        long startTime = System.nanoTime();
        
        int newInsertions = bloomFilterService.addBatch(request.getFilterName(), request.getItems());
        
        long latencyMicros = (System.nanoTime() - startTime) / 1000;
        
        BatchAddResponse response = new BatchAddResponse(
                request.getFilterName(), request.getItems().size(), newInsertions, latencyMicros);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/multi-check")
    @Timed(value = "api.bloom.multi_check", description = "Multi-filter check endpoint timing")
    public ResponseEntity<ApiResponse<MultiFilterCheckResponse>> checkMultipleFilters(
            @Valid @RequestBody MultiFilterCheckRequest request) {
        long startTime = System.nanoTime();
        
        Map<String, Boolean> results = bloomFilterService.checkMultipleFilters(
                request.getFilterNames(), request.getItem());
        
        long latencyMicros = (System.nanoTime() - startTime) / 1000;
        
        MultiFilterCheckResponse response = new MultiFilterCheckResponse(
                request.getItem(), results, latencyMicros);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
