package com.bloomguard.controller;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController implements HealthIndicator {

    private final RedissonClient redissonClient;
    private final DataSource dataSource;

    @Autowired
    public HealthController(RedissonClient redissonClient, DataSource dataSource) {
        this.redissonClient = redissonClient;
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        boolean redisHealthy = checkRedis(details);
        boolean dbHealthy = checkDatabase(details);
        
        if (redisHealthy && dbHealthy) {
            return Health.up().withDetails(details).build();
        }
        return Health.down().withDetails(details).build();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        
        boolean redisHealthy = checkRedis(details);
        boolean dbHealthy = checkDatabase(details);
        
        response.put("status", redisHealthy && dbHealthy ? "UP" : "DOWN");
        response.put("components", details);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readinessCheck() {
        Map<String, Object> details = new HashMap<>();
        boolean redisHealthy = checkRedis(details);
        boolean dbHealthy = checkDatabase(details);
        
        if (redisHealthy && dbHealthy) {
            return ResponseEntity.ok(Map.of("status", "READY"));
        }
        return ResponseEntity.status(503).body(Map.of("status", "NOT_READY"));
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> livenessCheck() {
        return ResponseEntity.ok(Map.of("status", "ALIVE"));
    }

    private boolean checkRedis(Map<String, Object> details) {
        try {
            redissonClient.getBucket("health-check").set("ping");
            details.put("redis", Map.of("status", "UP"));
            return true;
        } catch (Exception e) {
            details.put("redis", Map.of("status", "DOWN", "error", e.getMessage()));
            return false;
        }
    }

    private boolean checkDatabase(Map<String, Object> details) {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(5)) {
                details.put("database", Map.of("status", "UP"));
                return true;
            }
            details.put("database", Map.of("status", "DOWN", "error", "Connection invalid"));
            return false;
        } catch (Exception e) {
            details.put("database", Map.of("status", "DOWN", "error", e.getMessage()));
            return false;
        }
    }
}
