package com.bloomguard.service;

import com.bloomguard.metrics.BloomFilterMetrics;
import com.bloomguard.model.dto.response.FraudCheckResponse;
import com.bloomguard.repository.StolenCardRepository;
import com.bloomguard.repository.RecentTransactionRepository;
import com.bloomguard.security.TenantContext;
import com.bloomguard.util.HashUtil;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FraudCheckService {

    private static final Logger log = LoggerFactory.getLogger(FraudCheckService.class);

    private static final String STOLEN_CARDS_FILTER = "stolen-cards";
    private static final String DUPLICATE_TXN_FILTER = "duplicate-transactions";
    private static final String INVOICE_FILTER = "invoice-payments";
    private static final String SUSPICIOUS_ACCOUNTS_FILTER = "suspicious-accounts";

    private final BloomFilterService bloomFilterService;
    private final StolenCardRepository stolenCardRepository;
    private final RecentTransactionRepository recentTransactionRepository;
    private final AuditService auditService;
    private final BloomFilterMetrics metrics;

    @Value("${bloomguard.fraud.verify-false-positives:true}")
    private boolean verifyFalsePositives;

    @Autowired
    public FraudCheckService(BloomFilterService bloomFilterService,
                            StolenCardRepository stolenCardRepository,
                            RecentTransactionRepository recentTransactionRepository,
                            AuditService auditService,
                            BloomFilterMetrics metrics) {
        this.bloomFilterService = bloomFilterService;
        this.stolenCardRepository = stolenCardRepository;
        this.recentTransactionRepository = recentTransactionRepository;
        this.auditService = auditService;
        this.metrics = metrics;
    }

    @Timed(value = "fraud.check.stolen_card", description = "Time to check stolen card")
    public FraudCheckResponse checkStolenCard(String cardNumber) {
        long startTime = System.nanoTime();
        String cardHash = HashUtil.sha256(cardNumber);
        
        boolean bloomResult = bloomFilterService.mightContain(STOLEN_CARDS_FILTER, cardHash);
        
        if (!bloomResult) {
            long latency = (System.nanoTime() - startTime) / 1000;
            auditService.logFraudCheck("STOLEN_CARD", cardHash, false, false, latency);
            return FraudCheckResponse.clean("STOLEN_CARD", latency);
        }

        boolean verified = false;
        if (verifyFalsePositives) {
            String tenantId = TenantContext.getCurrentTenantId();
            verified = stolenCardRepository.existsByCardHashAndTenantIdAndActiveTrue(cardHash, tenantId);
            
            if (!verified) {
                metrics.recordFalsePositive(STOLEN_CARDS_FILTER);
            }
        }

        long latency = (System.nanoTime() - startTime) / 1000;
        auditService.logFraudCheck("STOLEN_CARD", cardHash, true, verified, latency);
        
        if (verified) {
            return FraudCheckResponse.flaggedVerified("STOLEN_CARD", latency);
        }
        return FraudCheckResponse.flaggedUnverified("STOLEN_CARD", latency);
    }

    @Timed(value = "fraud.check.duplicate_txn", description = "Time to check duplicate transaction")
    public FraudCheckResponse checkDuplicateTransaction(String fingerprint) {
        long startTime = System.nanoTime();
        String fingerprintHash = HashUtil.sha256(fingerprint);
        
        boolean bloomResult = bloomFilterService.mightContain(DUPLICATE_TXN_FILTER, fingerprintHash);
        
        if (!bloomResult) {
            bloomFilterService.add(DUPLICATE_TXN_FILTER, fingerprintHash);
            long latency = (System.nanoTime() - startTime) / 1000;
            auditService.logFraudCheck("DUPLICATE_TXN", fingerprintHash, false, false, latency);
            return FraudCheckResponse.clean("DUPLICATE_TRANSACTION", latency);
        }

        boolean verified = false;
        if (verifyFalsePositives) {
            String tenantId = TenantContext.getCurrentTenantId();
            verified = recentTransactionRepository.existsByFingerprintAndTenantId(fingerprintHash, tenantId);
            
            if (!verified) {
                metrics.recordFalsePositive(DUPLICATE_TXN_FILTER);
            }
        }

        long latency = (System.nanoTime() - startTime) / 1000;
        auditService.logFraudCheck("DUPLICATE_TXN", fingerprintHash, true, verified, latency);
        
        if (verified) {
            return FraudCheckResponse.flaggedVerified("DUPLICATE_TRANSACTION", latency);
        }
        return FraudCheckResponse.flaggedUnverified("DUPLICATE_TRANSACTION", latency);
    }

    @Timed(value = "fraud.check.invoice", description = "Time to check duplicate invoice")
    public FraudCheckResponse checkDuplicateInvoice(String fingerprint) {
        long startTime = System.nanoTime();
        String fingerprintHash = HashUtil.sha256(fingerprint);
        
        BloomFilterService.CheckAndAddResult result = bloomFilterService.checkAndAdd(INVOICE_FILTER, fingerprintHash);
        
        long latency = (System.nanoTime() - startTime) / 1000;
        
        if (!result.existedBefore()) {
            auditService.logFraudCheck("INVOICE_CHECK", fingerprintHash, false, false, latency);
            return FraudCheckResponse.clean("INVOICE_PAYMENT", latency);
        }

        auditService.logFraudCheck("INVOICE_CHECK", fingerprintHash, true, false, latency);
        return FraudCheckResponse.flaggedUnverified("INVOICE_PAYMENT", latency);
    }

    @Timed(value = "fraud.check.account", description = "Time to check suspicious account")
    public FraudCheckResponse checkSuspiciousAccount(String accountId) {
        long startTime = System.nanoTime();
        String accountHash = HashUtil.sha256(accountId);
        
        boolean bloomResult = bloomFilterService.mightContain(SUSPICIOUS_ACCOUNTS_FILTER, accountHash);
        
        long latency = (System.nanoTime() - startTime) / 1000;
        
        if (!bloomResult) {
            auditService.logFraudCheck("SUSPICIOUS_ACCOUNT", accountHash, false, false, latency);
            return FraudCheckResponse.clean("SUSPICIOUS_ACCOUNT", latency);
        }

        auditService.logFraudCheck("SUSPICIOUS_ACCOUNT", accountHash, true, false, latency);
        return FraudCheckResponse.flaggedUnverified("SUSPICIOUS_ACCOUNT", latency);
    }
}
