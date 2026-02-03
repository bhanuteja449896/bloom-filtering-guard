package com.bloomguard.service;

import com.bloomguard.metrics.BloomFilterMetrics;
import com.bloomguard.model.dto.response.FraudCheckResponse;
import com.bloomguard.repository.RecentTransactionRepository;
import com.bloomguard.repository.StolenCardRepository;
import com.bloomguard.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudCheckServiceTest {

    @Mock
    private BloomFilterService bloomFilterService;

    @Mock
    private StolenCardRepository stolenCardRepository;

    @Mock
    private RecentTransactionRepository recentTransactionRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private BloomFilterMetrics metrics;

    private FraudCheckService fraudCheckService;

    @BeforeEach
    void setUp() {
        fraudCheckService = new FraudCheckService(
                bloomFilterService,
                stolenCardRepository,
                recentTransactionRepository,
                auditService,
                metrics
        );
    }

    @Test
    void checkStolenCard_shouldReturnCleanForNonStolenCard() {
        try (MockedStatic<TenantContext> tenantContext = mockStatic(TenantContext.class)) {
            tenantContext.when(TenantContext::getCurrentTenantId).thenReturn("default");
            
            when(bloomFilterService.mightContain(anyString(), anyString())).thenReturn(false);

            FraudCheckResponse response = fraudCheckService.checkStolenCard("4111111111111111");

            assertFalse(response.isFlagged());
            assertEquals("LOW", response.getRiskLevel());
            assertEquals("STOLEN_CARD", response.getCheckType());
        }
    }

    @Test
    void checkStolenCard_shouldReturnFlaggedForPotentiallyStolenCard() {
        try (MockedStatic<TenantContext> tenantContext = mockStatic(TenantContext.class)) {
            tenantContext.when(TenantContext::getCurrentTenantId).thenReturn("default");
            
            when(bloomFilterService.mightContain(anyString(), anyString())).thenReturn(true);
            when(stolenCardRepository.existsByCardHashAndTenantIdAndActiveTrue(anyString(), anyString()))
                    .thenReturn(false);

            FraudCheckResponse response = fraudCheckService.checkStolenCard("4111111111111111");

            assertTrue(response.isFlagged());
            assertFalse(response.isVerified());
            assertEquals("MEDIUM", response.getRiskLevel());
        }
    }

    @Test
    void checkDuplicateTransaction_shouldReturnCleanForNewTransaction() {
        try (MockedStatic<TenantContext> tenantContext = mockStatic(TenantContext.class)) {
            tenantContext.when(TenantContext::getCurrentTenantId).thenReturn("default");
            
            when(bloomFilterService.mightContain(anyString(), anyString())).thenReturn(false);

            String fingerprint = "ACC123|100.00|USD|RECIPIENT";
            FraudCheckResponse response = fraudCheckService.checkDuplicateTransaction(fingerprint);

            assertFalse(response.isFlagged());
            assertEquals("LOW", response.getRiskLevel());
            
            verify(bloomFilterService).add(anyString(), anyString());
        }
    }

    @Test
    void checkDuplicateTransaction_shouldReturnFlaggedForDuplicate() {
        try (MockedStatic<TenantContext> tenantContext = mockStatic(TenantContext.class)) {
            tenantContext.when(TenantContext::getCurrentTenantId).thenReturn("default");
            
            when(bloomFilterService.mightContain(anyString(), anyString())).thenReturn(true);
            when(recentTransactionRepository.existsByFingerprintAndTenantId(anyString(), anyString()))
                    .thenReturn(true);

            String fingerprint = "ACC123|100.00|USD|RECIPIENT";
            FraudCheckResponse response = fraudCheckService.checkDuplicateTransaction(fingerprint);

            assertTrue(response.isFlagged());
        }
    }
}
