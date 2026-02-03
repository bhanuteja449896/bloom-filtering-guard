package com.bloomguard.service;

import com.bloomguard.model.entity.FilterConfiguration;
import com.bloomguard.repository.FilterConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RotationService {

    private static final Logger log = LoggerFactory.getLogger(RotationService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FilterConfigRepository filterConfigRepository;
    private final BloomFilterService bloomFilterService;
    private final BackupRecoveryService backupRecoveryService;

    @Autowired
    public RotationService(FilterConfigRepository filterConfigRepository,
                          BloomFilterService bloomFilterService,
                          BackupRecoveryService backupRecoveryService) {
        this.filterConfigRepository = filterConfigRepository;
        this.bloomFilterService = bloomFilterService;
        this.backupRecoveryService = backupRecoveryService;
    }

    public void rotateFilters() {
        List<FilterConfiguration> rotatableFilters = filterConfigRepository.findByRotatableTrue();
        
        for (FilterConfiguration config : rotatableFilters) {
            try {
                rotateFilter(config);
            } catch (Exception e) {
                log.error("Failed to rotate filter: {}", config.getFilterName(), e);
            }
        }
    }

    private void rotateFilter(FilterConfiguration config) {
        String baseFilterName = config.getFilterName();
        String todayDate = LocalDate.now().format(DATE_FORMAT);
        String newFilterName = baseFilterName + "-" + todayDate;
        
        if (!bloomFilterService.filterExists(newFilterName)) {
            bloomFilterService.createFilter(
                    newFilterName,
                    config.getExpectedInsertions(),
                    config.getFalsePositiveRate()
            );
            log.info("Created rotated filter: {}", newFilterName);
        }
        
        if (config.getRotationDays() != null && config.getRotationDays() > 0) {
            cleanupOldFilters(baseFilterName, config.getRotationDays());
        }
    }

    private void cleanupOldFilters(String baseFilterName, int retentionDays) {
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        
        for (int i = retentionDays + 1; i <= retentionDays + 7; i++) {
            LocalDate dateToClean = LocalDate.now().minusDays(i);
            String oldFilterName = baseFilterName + "-" + dateToClean.format(DATE_FORMAT);
            
            if (bloomFilterService.filterExists(oldFilterName)) {
                bloomFilterService.clear(oldFilterName);
                backupRecoveryService.clearBackup(oldFilterName);
                log.info("Cleaned up old filter: {}", oldFilterName);
            }
        }
    }

    public String getCurrentDatedFilterName(String baseFilterName) {
        String todayDate = LocalDate.now().format(DATE_FORMAT);
        return baseFilterName + "-" + todayDate;
    }
}
