package com.bloomguard.model.dto.response;

public class AddResponse {

    private String filterName;
    private boolean added;
    private long latencyMicros;

    public AddResponse() {}

    public AddResponse(String filterName, boolean added, long latencyMicros) {
        this.filterName = filterName;
        this.added = added;
        this.latencyMicros = latencyMicros;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    public long getLatencyMicros() {
        return latencyMicros;
    }

    public void setLatencyMicros(long latencyMicros) {
        this.latencyMicros = latencyMicros;
    }
}
