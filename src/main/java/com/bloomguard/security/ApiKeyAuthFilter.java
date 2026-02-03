package com.bloomguard.security;

import com.bloomguard.model.entity.ApiKey;
import com.bloomguard.repository.ApiKeyRepository;
import com.bloomguard.util.HashUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String CACHE_NAME = "apiKeys";

    private final ApiKeyRepository apiKeyRepository;
    private final CacheManager cacheManager;

    @Value("${bloomguard.security.api-key-enabled:true}")
    private boolean apiKeyEnabled;

    @Value("${bloomguard.security.default-api-key:}")
    private String defaultApiKey;

    @Autowired
    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository, CacheManager cacheManager) {
        this.apiKeyRepository = apiKeyRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        if (!apiKeyEnabled) {
            TenantContext.setCurrentTenantId("default");
            setAuthentication("default", "default");
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        
        if (apiKey == null || apiKey.isEmpty()) {
            if (!defaultApiKey.isEmpty()) {
                TenantContext.setCurrentTenantId("default");
                setAuthentication("default", "default");
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing API key\",\"message\":\"X-API-Key header is required\"}");
            return;
        }

        String keyHash = HashUtil.sha256(apiKey);
        
        ApiKey cachedKey = getCachedApiKey(keyHash);
        if (cachedKey != null) {
            processValidKey(cachedKey, request, response, filterChain);
            return;
        }

        Optional<ApiKey> apiKeyEntity = apiKeyRepository.findByKeyHashAndActiveTrue(keyHash);
        
        if (apiKeyEntity.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid API key\",\"message\":\"The provided API key is not valid\"}");
            return;
        }

        ApiKey key = apiKeyEntity.get();
        
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(Instant.now())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Expired API key\",\"message\":\"The API key has expired\"}");
            return;
        }

        cacheApiKey(keyHash, key);
        apiKeyRepository.updateLastUsedAt(keyHash, Instant.now());
        
        processValidKey(key, request, response, filterChain);
    }

    private void processValidKey(ApiKey key, HttpServletRequest request, HttpServletResponse response, 
                                FilterChain filterChain) throws ServletException, IOException {
        TenantContext.setCurrentTenantId(key.getTenantId());
        setAuthentication(key.getTenantId(), key.getName());
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void setAuthentication(String tenantId, String keyName) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_API_USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                tenantId, null, authorities);
        auth.setDetails(keyName);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private ApiKey getCachedApiKey(String keyHash) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(keyHash);
            if (wrapper != null) {
                return (ApiKey) wrapper.get();
            }
        }
        return null;
    }

    private void cacheApiKey(String keyHash, ApiKey key) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(keyHash, key);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || 
               path.startsWith("/actuator/prometheus") ||
               path.startsWith("/actuator/info");
    }
}
