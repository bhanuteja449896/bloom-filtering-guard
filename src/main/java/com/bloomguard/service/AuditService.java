package com.bloomguard.service;

import com.bloomguard.model.entity.AuditLog;
import com.bloomguard.repository.AuditLogRepository;
import com.bloomguard.security.TenantContext;
import com.bloomguard.util.HashUtil;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final Tracer tracer;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository, Tracer tracer) {
        this.auditLogRepository = auditLogRepository;
        this.tracer = tracer;
    }

    @Async("auditExecutor")
    public void logOperation(String filterName, String operation, String item, String result, long latencyMicros) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setFilterName(filterName);
            auditLog.setOperation(operation);
            auditLog.setItemHash(HashUtil.sha256(item));
            auditLog.setResult(result);
            auditLog.setTenantId(TenantContext.getCurrentTenantId());
            auditLog.setLatencyMicros(latencyMicros);
            auditLog.setCreatedAt(Instant.now());
            
            if (tracer.currentSpan() != null) {
                auditLog.setTraceId(tracer.currentSpan().context().traceId());
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log for filter={}, operation={}", filterName, operation, e);
        }
    }

    @Async("auditExecutor")
    public void logCheck(String filterName, String item, boolean found, long latencyMicros) {
        logOperation(filterName, "CHECK", item, found ? "FOUND" : "NOT_FOUND", latencyMicros);
    }

    @Async("auditExecutor")
    public void logAdd(String filterName, String item, boolean added, long latencyMicros) {
        logOperation(filterName, "ADD", item, added ? "ADDED" : "EXISTED", latencyMicros);
    }

    @Async("auditExecutor")
    public void logFraudCheck(String checkType, String item, boolean flagged, boolean verified, long latencyMicros) {
        String result = flagged ? (verified ? "FLAGGED_VERIFIED" : "FLAGGED_UNVERIFIED") : "CLEAN";
        logOperation(checkType, "FRAUD_CHECK", item, result, latencyMicros);
    }
}
