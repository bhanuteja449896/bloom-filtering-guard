package com.bloomguard.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stolen_cards", indexes = {
    @Index(name = "idx_stolen_cards_hash", columnList = "cardHash", unique = true),
    @Index(name = "idx_stolen_cards_tenant_id", columnList = "tenantId")
})
public class StolenCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String cardHash;

    @Column(nullable = false, length = 100)
    private String tenantId;

    @Column(length = 100)
    private String reportedBy;

    @Column(nullable = false)
    private Instant reportedAt;

    @Column
    private Instant verifiedAt;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (reportedAt == null) {
            reportedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardHash() {
        return cardHash;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Instant reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
