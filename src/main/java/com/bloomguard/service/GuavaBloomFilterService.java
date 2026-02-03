package com.bloomguard.service;

import com.bloomguard.exception.FilterNotFoundException;
import com.bloomguard.model.dto.response.BloomFilterStats;
import com.bloomguard.model.entity.FilterConfiguration;
import com.bloomguard.repository.FilterConfigRepository;
import com.bloomguard.security.TenantContext;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("guavaBloomFilterService")
public class GuavaBloomFilterService implements BloomFilterService {

    private static final Logger log = LoggerFactory.getLogger(GuavaBloomFilterService.class);

    private final FilterConfigRepository filterConfigRepository;
    private final Map<String, BloomFilter<CharSequence>> filterCache = new ConcurrentHashMap<>();
    private final Map<String, FilterConfiguration> configCache = new ConcurrentHashMap<>();

    @Autowired
    public GuavaBloomFilterService(FilterConfigRepository filterConfigRepository) {
        this.filterConfigRepository = filterConfigRepository;
    }

    @Override
    public boolean mightContain(String filterName, String item) {
        BloomFilter<CharSequence> filter = getOrCreateFilter(filterName);
        return filter.mightContain(item);
    }

    @Override
    public boolean add(String filterName, String item) {
        BloomFilter<CharSequence> filter = getOrCreateFilter(filterName);
        return filter.put(item);
    }

    @Override
    public CheckAndAddResult checkAndAdd(String filterName, String item) {
        BloomFilter<CharSequence> filter = getOrCreateFilter(filterName);
        boolean existedBefore = filter.mightContain(item);
        boolean added = false;
        if (!existedBefore) {
            added = filter.put(item);
        }
        return new CheckAndAddResult(existedBefore, added);
    }

    @Override
    public Map<String, Boolean> mightContainBatch(String filterName, List<String> items) {
        BloomFilter<CharSequence> filter = getOrCreateFilter(filterName);
        Map<String, Boolean> results = new HashMap<>();
        for (String item : items) {
            results.put(item, filter.mightContain(item));
        }
        return results;
    }

    @Override
    public int addBatch(String filterName, List<String> items) {
        BloomFilter<CharSequence> filter = getOrCreateFilter(filterName);
        int added = 0;
        for (String item : items) {
            if (filter.put(item)) {
                added++;
            }
        }
        return added;
    }

    @Override
    public BloomFilterStats getStats(String filterName) {
        String fullFilterName = getFullFilterName(filterName);
        FilterConfiguration config = configCache.get(fullFilterName);
        
        if (config == null) {
            config = filterConfigRepository.findByFilterName(fullFilterName)
                    .orElseThrow(() -> new FilterNotFoundException(filterName));
            configCache.put(fullFilterName, config);
        }

        BloomFilter<CharSequence> filter = filterCache.get(fullFilterName);

        BloomFilterStats stats = new BloomFilterStats();
        stats.setFilterName(filterName);
        stats.setExpectedInsertions(config.getExpectedInsertions());
        stats.setFalsePositiveRate(config.getFalsePositiveRate());
        stats.setTenantId(config.getTenantId());

        if (filter != null) {
            stats.setApproximateElementCount(filter.approximateElementCount());
            double fillRatio = (double) filter.approximateElementCount() / config.getExpectedInsertions();
            stats.setFillRatio(fillRatio);
            stats.setHealthy(fillRatio < 0.9);
        } else {
            stats.setApproximateElementCount(0);
            stats.setFillRatio(0.0);
            stats.setHealthy(true);
        }

        return stats;
    }

    @Override
    public void clear(String filterName) {
        String fullFilterName = getFullFilterName(filterName);
        filterCache.remove(fullFilterName);
        configCache.remove(fullFilterName);
        log.info("Cleared in-memory bloom filter: {}", filterName);
    }

    @Override
    public void createFilter(String filterName, long expectedInsertions, double falsePositiveRate) {
        String fullFilterName = getFullFilterName(filterName);
        
        if (filterCache.containsKey(fullFilterName)) {
            log.warn("Filter already exists in memory: {}", filterName);
            return;
        }

        BloomFilter<CharSequence> filter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions,
                falsePositiveRate
        );
        filterCache.put(fullFilterName, filter);
        
        log.info("Created in-memory bloom filter: {} with expectedInsertions={}, fpp={}", 
                filterName, expectedInsertions, falsePositiveRate);
    }

    @Override
    public boolean filterExists(String filterName) {
        String fullFilterName = getFullFilterName(filterName);
        return filterCache.containsKey(fullFilterName);
    }

    @Override
    public Map<String, Boolean> checkMultipleFilters(List<String> filterNames, String item) {
        Map<String, Boolean> results = new HashMap<>();
        for (String filterName : filterNames) {
            try {
                results.put(filterName, mightContain(filterName, item));
            } catch (Exception e) {
                results.put(filterName, false);
            }
        }
        return results;
    }

    private BloomFilter<CharSequence> getOrCreateFilter(String filterName) {
        String fullFilterName = getFullFilterName(filterName);
        return filterCache.computeIfAbsent(fullFilterName, name -> {
            FilterConfiguration config = filterConfigRepository.findByFilterName(name)
                    .orElseGet(() -> getDefaultConfig(name));
            
            configCache.put(name, config);
            
            return BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8),
                    config.getExpectedInsertions(),
                    config.getFalsePositiveRate()
            );
        });
    }

    private String getFullFilterName(String filterName) {
        String tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            return filterName;
        }
        return tenantId + ":" + filterName;
    }

    private FilterConfiguration getDefaultConfig(String filterName) {
        FilterConfiguration config = new FilterConfiguration();
        config.setFilterName(filterName);
        config.setExpectedInsertions(1_000_000L);
        config.setFalsePositiveRate(0.01);
        config.setTenantId(TenantContext.getCurrentTenantId());
        return config;
    }
}
