package com.bloomguard.controller;

import com.bloomguard.model.dto.response.ApiResponse;
import com.bloomguard.model.dto.response.BloomFilterStats;
import com.bloomguard.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bloom/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/filter/{filterName}")
    public ResponseEntity<ApiResponse<BloomFilterStats>> getFilterStats(@PathVariable String filterName) {
        BloomFilterStats stats = statisticsService.getFilterStats(filterName);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/operations/{filterName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOperationStats(@PathVariable String filterName) {
        Map<String, Object> stats = statisticsService.getOperationStats(filterName);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/system")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        Map<String, Object> stats = statisticsService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
