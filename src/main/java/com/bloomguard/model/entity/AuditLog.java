package com.bloomguard.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_filter_name", columnList = "filterName"),
    @Index(name = "idx_audit_logs_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_audit_logs_created_at", columnList = "createdAt")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String filterName;

    @Column(nullable = false, length = 50)
    private String operation;

    @Column(nullable = false, length = 64)
    private String itemHash;

    @Column(nullable = false, length = 20)
    private String result;

    @Column(nullable = false, length = 100)
    private String tenantId;

    @Column(nullable = false)
    private Long latencyMicros;

    @Column(length = 64)
    private String traceId;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getItemHash() {
        return itemHash;
    }

    public void setItemHash(String itemHash) {
        this.itemHash = itemHash;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getLatencyMicros() {
        return latencyMicros;
    }

    public void setLatencyMicros(Long latencyMicros) {
        this.latencyMicros = latencyMicros;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
