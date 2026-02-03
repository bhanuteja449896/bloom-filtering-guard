package com.bloomguard.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bloom_filter_backup", indexes = {
    @Index(name = "idx_backup_filter_name", columnList = "filterName"),
    @Index(name = "idx_backup_tenant_id", columnList = "tenantId")
})
public class BloomFilterBackup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String filterName;

    @Column(nullable = false, length = 64)
    private String itemHash;

    @Column(nullable = false, length = 100)
    private String tenantId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant expiresAt;

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

    public String getItemHash() {
        return itemHash;
    }

    public void setItemHash(String itemHash) {
        this.itemHash = itemHash;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
