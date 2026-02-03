package com.bloomguard.model.dto.response;

import java.util.Map;

public class MultiFilterCheckResponse {

    private String item;
    private Map<String, Boolean> results;
    private boolean anyMatch;
    private long latencyMicros;

    public MultiFilterCheckResponse() {}

    public MultiFilterCheckResponse(String item, Map<String, Boolean> results, long latencyMicros) {
        this.item = item;
        this.results = results;
        this.anyMatch = results != null && results.values().stream().anyMatch(v -> v);
        this.latencyMicros = latencyMicros;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Map<String, Boolean> getResults() {
        return results;
    }

    public void setResults(Map<String, Boolean> results) {
        this.results = results;
        this.anyMatch = results != null && results.values().stream().anyMatch(v -> v);
    }

    public boolean isAnyMatch() {
        return anyMatch;
    }

    public void setAnyMatch(boolean anyMatch) {
        this.anyMatch = anyMatch;
    }

    public long getLatencyMicros() {
        return latencyMicros;
    }

    public void setLatencyMicros(long latencyMicros) {
        this.latencyMicros = latencyMicros;
    }
}
