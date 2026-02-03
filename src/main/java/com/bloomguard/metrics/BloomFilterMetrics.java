package com.bloomguard.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class BloomFilterMetrics {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> checkCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> addCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> falsePositiveCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> operationTimers = new ConcurrentHashMap<>();

    @Autowired
    public BloomFilterMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordCheck(String filterName, boolean found, long durationMicros) {
        getCheckCounter(filterName, found).increment();
        getOperationTimer(filterName, "check").record(durationMicros, TimeUnit.MICROSECONDS);
    }

    public void recordAdd(String filterName, boolean added, long durationMicros) {
        getAddCounter(filterName, added).increment();
        getOperationTimer(filterName, "add").record(durationMicros, TimeUnit.MICROSECONDS);
    }

    public void recordFalsePositive(String filterName) {
        getFalsePositiveCounter(filterName).increment();
    }

    private Counter getCheckCounter(String filterName, boolean found) {
        String key = filterName + "_" + (found ? "found" : "not_found");
        return checkCounters.computeIfAbsent(key, k ->
                Counter.builder("bloom_filter_checks_total")
                        .tag("filter_name", filterName)
                        .tag("result", found ? "found" : "not_found")
                        .description("Total bloom filter checks")
                        .register(meterRegistry));
    }

    private Counter getAddCounter(String filterName, boolean added) {
        String key = filterName + "_" + (added ? "added" : "existed");
        return addCounters.computeIfAbsent(key, k ->
                Counter.builder("bloom_filter_adds_total")
                        .tag("filter_name", filterName)
                        .tag("result", added ? "added" : "existed")
                        .description("Total bloom filter additions")
                        .register(meterRegistry));
    }

    private Counter getFalsePositiveCounter(String filterName) {
        return falsePositiveCounters.computeIfAbsent(filterName, k ->
                Counter.builder("bloom_filter_false_positives_total")
                        .tag("filter_name", filterName)
                        .description("Total confirmed false positives")
                        .register(meterRegistry));
    }

    private Timer getOperationTimer(String filterName, String operation) {
        String key = filterName + "_" + operation;
        return operationTimers.computeIfAbsent(key, k ->
                Timer.builder("bloom_filter_operation_duration_seconds")
                        .tag("filter_name", filterName)
                        .tag("operation", operation)
                        .description("Bloom filter operation duration")
                        .register(meterRegistry));
    }
}
