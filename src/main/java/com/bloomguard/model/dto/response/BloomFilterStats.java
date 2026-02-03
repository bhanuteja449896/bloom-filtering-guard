package com.bloomguard.model.dto.response;

public class BloomFilterStats {

    private String filterName;
    private long expectedInsertions;
    private double falsePositiveRate;
    private long approximateElementCount;
    private double fillRatio;
    private boolean healthy;
    private String tenantId;

    public BloomFilterStats() {}

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public long getExpectedInsertions() {
        return expectedInsertions;
    }

    public void setExpectedInsertions(long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public double getFalsePositiveRate() {
        return falsePositiveRate;
    }

    public void setFalsePositiveRate(double falsePositiveRate) {
        this.falsePositiveRate = falsePositiveRate;
    }

    public long getApproximateElementCount() {
        return approximateElementCount;
    }

    public void setApproximateElementCount(long approximateElementCount) {
        this.approximateElementCount = approximateElementCount;
    }

    public double getFillRatio() {
        return fillRatio;
    }

    public void setFillRatio(double fillRatio) {
        this.fillRatio = fillRatio;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
