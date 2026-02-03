package com.bloomguard.service;

import com.bloomguard.model.dto.response.BloomFilterStats;
import java.util.List;
import java.util.Map;

public interface BloomFilterService {

    boolean mightContain(String filterName, String item);

    boolean add(String filterName, String item);

    CheckAndAddResult checkAndAdd(String filterName, String item);

    Map<String, Boolean> mightContainBatch(String filterName, List<String> items);

    int addBatch(String filterName, List<String> items);

    BloomFilterStats getStats(String filterName);

    void clear(String filterName);

    void createFilter(String filterName, long expectedInsertions, double falsePositiveRate);

    boolean filterExists(String filterName);

    Map<String, Boolean> checkMultipleFilters(List<String> filterNames, String item);

    record CheckAndAddResult(boolean existedBefore, boolean addedNow) {}
}
