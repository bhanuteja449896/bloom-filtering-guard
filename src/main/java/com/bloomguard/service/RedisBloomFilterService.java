package com.bloomguard.service;

import com.bloomguard.exception.BloomFilterException;
import com.bloomguard.exception.FilterNotFoundException;
import com.bloomguard.model.dto.response.BloomFilterStats;
import com.bloomguard.model.entity.FilterConfiguration;
import com.bloomguard.repository.FilterConfigRepository;
import com.bloomguard.security.TenantContext;
import io.micrometer.core.annotation.Timed;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisBloomFilterService implements BloomFilterService {

    private static final Logger log = LoggerFactory.getLogger(RedisBloomFilterService.class);

    private final RedissonClient redissonClient;
    private final FilterConfigRepository filterConfigRepository;
    private final Map<String, RBloomFilter<String>> filterCache = new ConcurrentHashMap<>();

    @Autowired
    public RedisBloomFilterService(RedissonClient redissonClient, FilterConfigRepository filterConfigRepository) {
        this.redissonClient = redissonClient;
        this.filterConfigRepository = filterConfigRepository;
    }

    @Override
    @Timed(value = "bloom.filter.check", description = "Time to check item in bloom filter")
    public boolean mightContain(String filterName, String item) {
        RBloomFilter<String> filter = getOrCreateFilter(filterName);
        return filter.contains(item);
    }

    @Override
    @Timed(value = "bloom.filter.add", description = "Time to add item to bloom filter")
    public boolean add(String filterName, String item) {
        RBloomFilter<String> filter = getOrCreateFilter(filterName);
        return filter.add(item);
    }

    @Override
    @Timed(value = "bloom.filter.check_and_add", description = "Time to check and add item")
    public CheckAndAddResult checkAndAdd(String filterName, String item) {
        RBloomFilter<String> filter = getOrCreateFilter(filterName);
        boolean existedBefore = filter.contains(item);
        boolean added = false;
        if (!existedBefore) {
            added = filter.add(item);
        }
        return new CheckAndAddResult(existedBefore, added);
    }

    @Override
    @Timed(value = "bloom.filter.batch_check", description = "Time to batch check items")
    public Map<String, Boolean> mightContainBatch(String filterName, List<String> items) {
        RBloomFilter<String> filter = getOrCreateFilter(filterName);
        Map<String, Boolean> results = new HashMap<>();
        for (String item : items) {
            results.put(item, filter.contains(item));
        }
        return results;
    }

    @Override
    @Timed(value = "bloom.filter.batch_add", description = "Time to batch add items")
    public int addBatch(String filterName, List<String> items) {
        RBloomFilter<String> filter = getOrCreateFilter(filterName);
        int added = 0;
        for (String item : items) {
            if (filter.add(item)) {
                added++;
            }
        }
        return added;
    }

    @Override
    public BloomFilterStats getStats(String filterName) {
        String fullFilterName = getFullFilterName(filterName);
        FilterConfiguration config = filterConfigRepository.findByFilterName(fullFilterName)
                .orElseThrow(() -> new FilterNotFoundException(filterName));

        RBloomFilter<String> filter = filterCache.get(fullFilterName);

        BloomFilterStats stats = new BloomFilterStats();
        stats.setFilterName(filterName);
        stats.setExpectedInsertions(config.getExpectedInsertions());
        stats.setFalsePositiveRate(config.getFalsePositiveRate());
        stats.setTenantId(config.getTenantId());

        if (filter != null && filter.isExists()) {
            stats.setApproximateElementCount(filter.count());
            double fillRatio = (double) filter.count() / config.getExpectedInsertions();
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
        RBloomFilter<String> filter = filterCache.get(fullFilterName);
        if (filter != null) {
            filter.delete();
            filterCache.remove(fullFilterName);
        }
        log.info("Cleared bloom filter: {}", filterName);
    }

    @Override
    public void createFilter(String filterName, long expectedInsertions, double falsePositiveRate) {
        String fullFilterName = getFullFilterName(filterName);
        RBloomFilter<String> filter = redissonClient.getBloomFilter(fullFilterName);
        
        if (filter.isExists()) {
            throw new BloomFilterException("Filter already exists: " + filterName);
        }

        boolean initialized = filter.tryInit(expectedInsertions, falsePositiveRate);
        if (!initialized) {
            throw new BloomFilterException("Failed to initialize filter: " + filterName);
        }

        filterCache.put(fullFilterName, filter);
        log.info("Created bloom filter: {} with expectedInsertions={}, fpp={}", 
                filterName, expectedInsertions, falsePositiveRate);
    }

    @Override
    public boolean filterExists(String filterName) {
        String fullFilterName = getFullFilterName(filterName);
        RBloomFilter<String> filter = redissonClient.getBloomFilter(fullFilterName);
        return filter.isExists();
    }

    @Override
    public Map<String, Boolean> checkMultipleFilters(List<String> filterNames, String item) {
        Map<String, Boolean> results = new HashMap<>();
        for (String filterName : filterNames) {
            try {
                results.put(filterName, mightContain(filterName, item));
            } catch (FilterNotFoundException e) {
                results.put(filterName, false);
            }
        }
        return results;
    }

    private RBloomFilter<String> getOrCreateFilter(String filterName) {
        String fullFilterName = getFullFilterName(filterName);
        return filterCache.computeIfAbsent(fullFilterName, name -> {
            RBloomFilter<String> filter = redissonClient.getBloomFilter(name);
            
            if (!filter.isExists()) {
                FilterConfiguration config = filterConfigRepository.findByFilterName(name)
                        .orElseGet(() -> getDefaultConfig(filterName));
                
                filter.tryInit(config.getExpectedInsertions(), config.getFalsePositiveRate());
            }
            
            return filter;
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
