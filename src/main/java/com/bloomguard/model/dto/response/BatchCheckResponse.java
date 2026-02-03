package com.bloomguard.model.dto.response;

import java.util.List;
import java.util.Map;

public class BatchCheckResponse {

    private String filterName;
    private Map<String, Boolean> results;
    private int totalChecked;
    private int foundCount;
    private long latencyMicros;

    public BatchCheckResponse() {}

    public BatchCheckResponse(String filterName, Map<String, Boolean> results, long latencyMicros) {
        this.filterName = filterName;
        this.results = results;
        this.totalChecked = results.size();
        this.foundCount = (int) results.values().stream().filter(v -> v).count();
        this.latencyMicros = latencyMicros;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public Map<String, Boolean> getResults() {
        return results;
    }

    public void setResults(Map<String, Boolean> results) {
        this.results = results;
        if (results != null) {
            this.totalChecked = results.size();
            this.foundCount = (int) results.values().stream().filter(v -> v).count();
        }
    }

    public int getTotalChecked() {
        return totalChecked;
    }

    public void setTotalChecked(int totalChecked) {
        this.totalChecked = totalChecked;
    }

    public int getFoundCount() {
        return foundCount;
    }

    public void setFoundCount(int foundCount) {
        this.foundCount = foundCount;
    }

    public long getLatencyMicros() {
        return latencyMicros;
    }

    public void setLatencyMicros(long latencyMicros) {
        this.latencyMicros = latencyMicros;
    }
}
