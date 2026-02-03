package com.bloomguard.controller;

import com.bloomguard.model.dto.request.*;
import com.bloomguard.model.dto.response.ApiResponse;
import com.bloomguard.model.dto.response.FraudCheckResponse;
import com.bloomguard.service.FraudCheckService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bloom/fraud")
public class FraudDetectionController {

    private final FraudCheckService fraudCheckService;

    @Autowired
    public FraudDetectionController(FraudCheckService fraudCheckService) {
        this.fraudCheckService = fraudCheckService;
    }

    @PostMapping("/check-stolen-card")
    @Timed(value = "api.fraud.stolen_card", description = "Stolen card check endpoint timing")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> checkStolenCard(
            @Valid @RequestBody StolenCardCheckRequest request) {
        FraudCheckResponse response = fraudCheckService.checkStolenCard(request.getCardNumber());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/check-duplicate-txn")
    @Timed(value = "api.fraud.duplicate_txn", description = "Duplicate transaction check endpoint timing")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> checkDuplicateTransaction(
            @Valid @RequestBody DuplicateTransactionRequest request) {
        FraudCheckResponse response = fraudCheckService.checkDuplicateTransaction(request.toFingerprint());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/check-invoice")
    @Timed(value = "api.fraud.invoice", description = "Invoice check endpoint timing")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> checkInvoice(
            @Valid @RequestBody InvoiceCheckRequest request) {
        FraudCheckResponse response = fraudCheckService.checkDuplicateInvoice(request.toFingerprint());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/check-account")
    @Timed(value = "api.fraud.account", description = "Suspicious account check endpoint timing")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> checkSuspiciousAccount(
            @Valid @RequestBody AccountCheckRequest request) {
        FraudCheckResponse response = fraudCheckService.checkSuspiciousAccount(request.getAccountId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
