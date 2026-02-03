package com.bloomguard.scheduler;

import com.bloomguard.repository.AuditLogRepository;
import com.bloomguard.repository.BackupRepository;
import com.bloomguard.repository.RecentTransactionRepository;
import com.bloomguard.service.RotationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class BloomFilterScheduler {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterScheduler.class);

    private final RotationService rotationService;
    private final AuditLogRepository auditLogRepository;
    private final BackupRepository backupRepository;
    private final RecentTransactionRepository recentTransactionRepository;

    @Value("${bloomguard.cleanup.audit-retention-days:30}")
    private int auditRetentionDays;

    @Value("${bloomguard.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Autowired
    public BloomFilterScheduler(RotationService rotationService,
                               AuditLogRepository auditLogRepository,
                               BackupRepository backupRepository,
                               RecentTransactionRepository recentTransactionRepository) {
        this.rotationService = rotationService;
        this.auditLogRepository = auditLogRepository;
        this.backupRepository = backupRepository;
        this.recentTransactionRepository = recentTransactionRepository;
    }

    @Scheduled(cron = "${bloomguard.rotation.cron:0 0 0 * * ?}")
    public void rotateFilters() {
        log.info("Starting scheduled filter rotation");
        try {
            rotationService.rotateFilters();
            log.info("Completed scheduled filter rotation");
        } catch (Exception e) {
            log.error("Filter rotation failed", e);
        }
    }

    @Scheduled(cron = "${bloomguard.cleanup.cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupExpiredData() {
        if (!cleanupEnabled) {
            return;
        }

        log.info("Starting scheduled data cleanup");
        
        try {
            Instant auditCutoff = Instant.now().minus(auditRetentionDays, ChronoUnit.DAYS);
            auditLogRepository.deleteByCreatedAtBefore(auditCutoff);
            log.info("Cleaned up audit logs older than {} days", auditRetentionDays);
        } catch (Exception e) {
            log.error("Audit log cleanup failed", e);
        }

        try {
            backupRepository.deleteByExpiresAtBefore(Instant.now());
            log.info("Cleaned up expired backup entries");
        } catch (Exception e) {
            log.error("Backup cleanup failed", e);
        }

        try {
            recentTransactionRepository.deleteByExpiresAtBefore(Instant.now());
            log.info("Cleaned up expired transaction records");
        } catch (Exception e) {
            log.error("Transaction cleanup failed", e);
        }
    }
}
