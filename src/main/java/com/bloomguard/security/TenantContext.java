package com.bloomguard.security;

public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getCurrentTenantId() {
        String tenantId = currentTenant.get();
        return tenantId != null ? tenantId : "default";
    }

    public static void clear() {
        currentTenant.remove();
    }
}
