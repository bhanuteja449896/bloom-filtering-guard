package com.bloomguard.model.dto.response;

public class CheckAndAddResponse {

    private String filterName;
    private boolean existedBefore;
    private boolean addedNow;
    private long latencyMicros;

    public CheckAndAddResponse() {}

    public CheckAndAddResponse(String filterName, boolean existedBefore, boolean addedNow, long latencyMicros) {
        this.filterName = filterName;
        this.existedBefore = existedBefore;
        this.addedNow = addedNow;
        this.latencyMicros = latencyMicros;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public boolean isExistedBefore() {
        return existedBefore;
    }

    public void setExistedBefore(boolean existedBefore) {
        this.existedBefore = existedBefore;
    }

    public boolean isAddedNow() {
        return addedNow;
    }

    public void setAddedNow(boolean addedNow) {
        this.addedNow = addedNow;
    }

    public long getLatencyMicros() {
        return latencyMicros;
    }

    public void setLatencyMicros(long latencyMicros) {
        this.latencyMicros = latencyMicros;
    }
}
