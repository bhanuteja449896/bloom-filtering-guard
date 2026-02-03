package com.bloomguard.service;

import com.bloomguard.model.entity.BloomFilterBackup;
import com.bloomguard.repository.BackupRepository;
import com.bloomguard.security.TenantContext;
import com.bloomguard.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BackupRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(BackupRecoveryService.class);

    private final BackupRepository backupRepository;
    private final BloomFilterService bloomFilterService;

    @Autowired
    public BackupRecoveryService(BackupRepository backupRepository, BloomFilterService bloomFilterService) {
        this.backupRepository = backupRepository;
        this.bloomFilterService = bloomFilterService;
    }

    @Async("backupExecutor")
    public void backupItem(String filterName, String item, Integer expirationDays) {
        try {
            String tenantId = TenantContext.getCurrentTenantId();
            String itemHash = HashUtil.sha256(item);

            if (backupRepository.existsByFilterNameAndItemHashAndTenantId(filterName, itemHash, tenantId)) {
                return;
            }

            BloomFilterBackup backup = new BloomFilterBackup();
            backup.setFilterName(filterName);
            backup.setItemHash(itemHash);
            backup.setTenantId(tenantId);
            backup.setCreatedAt(Instant.now());

            if (expirationDays != null && expirationDays > 0) {
                backup.setExpiresAt(Instant.now().plus(expirationDays, ChronoUnit.DAYS));
            }

            backupRepository.save(backup);
        } catch (Exception e) {
            log.error("Failed to backup item for filter={}", filterName, e);
        }
    }

    @Transactional
    public long rebuildFromBackup(String filterName) {
        String tenantId = TenantContext.getCurrentTenantId();
        log.info("Starting rebuild of filter {} for tenant {}", filterName, tenantId);

        bloomFilterService.clear(filterName);

        AtomicLong count = new AtomicLong(0);
        
        backupRepository.streamByFilterNameAndTenantId(filterName, tenantId)
                .forEach(backup -> {
                    bloomFilterService.add(filterName, backup.getItemHash());
                    count.incrementAndGet();
                });

        log.info("Rebuilt filter {} with {} items for tenant {}", filterName, count.get(), tenantId);
        return count.get();
    }

    @Transactional
    public void clearBackup(String filterName) {
        String tenantId = TenantContext.getCurrentTenantId();
        backupRepository.deleteByFilterNameAndTenantId(filterName, tenantId);
        log.info("Cleared backup for filter {} tenant {}", filterName, tenantId);
    }

    public long getBackupCount(String filterName) {
        String tenantId = TenantContext.getCurrentTenantId();
        return backupRepository.countByFilterNameAndTenantId(filterName, tenantId);
    }
}
