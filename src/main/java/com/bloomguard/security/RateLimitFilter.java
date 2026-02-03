package com.bloomguard.security;

import com.bloomguard.exception.RateLimitExceededException;
import com.bloomguard.model.entity.ApiKey;
import com.bloomguard.repository.ApiKeyRepository;
import com.bloomguard.util.HashUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ApiKeyRepository apiKeyRepository;

    @Value("${bloomguard.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${bloomguard.rate-limit.default-limit:1000}")
    private int defaultLimit;

    @Autowired
    public RateLimitFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        String bucketKey = apiKey != null ? HashUtil.sha256(apiKey) : getClientIp(request);

        Bucket bucket = buckets.computeIfAbsent(bucketKey, this::createBucket);

        if (bucket.tryConsume(1)) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.addHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please retry after 60 seconds.\"}");
        }
    }

    private Bucket createBucket(String key) {
        int limit = defaultLimit;
        
        try {
            apiKeyRepository.findByKeyHashAndActiveTrue(key)
                    .ifPresent(apiKey -> {});
        } catch (Exception ignored) {
        }

        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }
}
