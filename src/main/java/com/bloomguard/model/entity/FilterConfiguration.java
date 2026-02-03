package com.bloomguard.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "filter_configurations")
public class FilterConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String filterName;

    @Column(nullable = false)
    private Long expectedInsertions;

    @Column(nullable = false)
    private Double falsePositiveRate;

    @Column(nullable = false, length = 100)
    private String tenantId;

    @Column(nullable = false)
    private boolean rotatable = false;

    @Column
    private Integer rotationDays;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
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

    public Long getExpectedInsertions() {
        return expectedInsertions;
    }

    public void setExpectedInsertions(Long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public Double getFalsePositiveRate() {
        return falsePositiveRate;
    }

    public void setFalsePositiveRate(Double falsePositiveRate) {
        this.falsePositiveRate = falsePositiveRate;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isRotatable() {
        return rotatable;
    }

    public void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }

    public Integer getRotationDays() {
        return rotationDays;
    }

    public void setRotationDays(Integer rotationDays) {
        this.rotationDays = rotationDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
