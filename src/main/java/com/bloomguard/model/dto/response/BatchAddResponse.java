package com.bloomguard.model.dto.response;

public class BatchAddResponse {

    private String filterName;
    private int totalAdded;
    private int newInsertions;
    private long latencyMicros;

    public BatchAddResponse() {}

    public BatchAddResponse(String filterName, int totalAdded, int newInsertions, long latencyMicros) {
        this.filterName = filterName;
        this.totalAdded = totalAdded;
        this.newInsertions = newInsertions;
        this.latencyMicros = latencyMicros;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public int getTotalAdded() {
        return totalAdded;
    }

    public void setTotalAdded(int totalAdded) {
        this.totalAdded = totalAdded;
    }

    public int getNewInsertions() {
        return newInsertions;
    }

    public void setNewInsertions(int newInsertions) {
        this.newInsertions = newInsertions;
    }

    public long getLatencyMicros() {
        return latencyMicros;
    }

    public void setLatencyMicros(long latencyMicros) {
        this.latencyMicros = latencyMicros;
    }
}
