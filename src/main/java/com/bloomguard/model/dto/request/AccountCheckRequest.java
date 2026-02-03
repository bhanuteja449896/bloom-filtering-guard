package com.bloomguard.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AccountCheckRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    public AccountCheckRequest() {}

    public AccountCheckRequest(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
