package com.bloomguard.model.dto.response;

public class CheckResponse {

    private String filterName;
    private boolean mightExist;
    private long latencyMicros;

    public CheckResponse() {}

    public CheckResponse(String filterName, boolean mightExist, long latencyMicros) {
        this.filterName = filterName;
        this.mightExist = mightExist;
        this.latencyMicros = latencyMicros;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public boolean isMightExist() {
        return mightExist;
    }

    public void setMightExist(boolean mightExist) {
        this.mightExist = mightExist;
    }

    public long getLatencyMicros() {
        return latencyMicros;
    }

    public void setLatencyMicros(long latencyMicros) {
        this.latencyMicros = latencyMicros;
    }
}
