package com.bloomguard.model.dto.response;

public class FraudCheckResponse {

    private String checkType;
    private boolean flagged;
    private boolean verified;
    private String riskLevel;
    private long latencyMicros;

    public FraudCheckResponse() {}

    public FraudCheckResponse(String checkType, boolean flagged, boolean verified, String riskLevel, long latencyMicros) {
        this.checkType = checkType;
        this.flagged = flagged;
        this.verified = verified;
        this.riskLevel = riskLevel;
        this.latencyMicros = latencyMicros;
    }

    public static FraudCheckResponse clean(String checkType, long latencyMicros) {
        return new FraudCheckResponse(checkType, false, false, "LOW", latencyMicros);
    }

    public static FraudCheckResponse flaggedUnverified(String checkType, long latencyMicros) {
        return new FraudCheckResponse(checkType, true, false, "MEDIUM", latencyMicros);
    }

    public static FraudCheckResponse flaggedVerified(String checkType, long latencyMicros) {
        return new FraudCheckResponse(checkType, true, true, "HIGH", latencyMicros);
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public long getLatencyMicros() {
        return latencyMicros;
    }

    public void setLatencyMicros(long latencyMicros) {
        this.latencyMicros = latencyMicros;
    }
}
