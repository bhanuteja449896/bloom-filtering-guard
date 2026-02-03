package com.bloomguard.service;

import com.bloomguard.model.dto.response.BloomFilterStats;
import com.bloomguard.repository.AuditLogRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    private final AuditLogRepository auditLogRepository;
    private final BloomFilterService bloomFilterService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public StatisticsService(AuditLogRepository auditLogRepository,
                            BloomFilterService bloomFilterService,
                            MeterRegistry meterRegistry) {
        this.auditLogRepository = auditLogRepository;
        this.bloomFilterService = bloomFilterService;
        this.meterRegistry = meterRegistry;
    }

    public BloomFilterStats getFilterStats(String filterName) {
        return bloomFilterService.getStats(filterName);
    }

    public Map<String, Object> getOperationStats(String filterName) {
        Map<String, Object> stats = new HashMap<>();
        
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        
        long checksTotal = auditLogRepository.countByFilterNameAndResult(filterName, "FOUND") +
                          auditLogRepository.countByFilterNameAndResult(filterName, "NOT_FOUND");
        long found = auditLogRepository.countByFilterNameAndResult(filterName, "FOUND");
        long added = auditLogRepository.countByFilterNameAndResult(filterName, "ADDED");
        
        Double avgLatency = auditLogRepository.getAverageLatency(filterName, since);
        
        stats.put("filterName", filterName);
        stats.put("totalChecks", checksTotal);
        stats.put("foundCount", found);
        stats.put("addedCount", added);
        stats.put("avgLatencyMicros", avgLatency != null ? avgLatency : 0.0);
        stats.put("hitRatio", checksTotal > 0 ? (double) found / checksTotal : 0.0);
        
        return stats;
    }

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("jvmMemoryUsed", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        stats.put("jvmMemoryMax", Runtime.getRuntime().maxMemory());
        stats.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        return stats;
    }
}
