package com.connectsoar.backend.service;

import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private static class TokenBucket {
        private final int maxTokens;
        private final long refillIntervalMs;
        private long lastRefillTimestamp;
        private final AtomicInteger tokens;

        public TokenBucket(int maxTokens, long refillIntervalMs) {
            this.maxTokens = maxTokens;
            this.refillIntervalMs = refillIntervalMs;
            this.lastRefillTimestamp = System.currentTimeMillis();
            this.tokens = new AtomicInteger(maxTokens);
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            if (now - lastRefillTimestamp > refillIntervalMs) {
                tokens.set(maxTokens);
                lastRefillTimestamp = now;
            }
        }
    }

    private final Map<String, TokenBucket> rateLimits = new ConcurrentHashMap<>();

    /**
     * Checks rate limit for a given key and action.
     * Throws ApiException with ErrorCode.RATE_LIMITED if exceeded.
     */
    public void checkRateLimit(String key, int maxRequestsPerMinute) {
        TokenBucket bucket = rateLimits.computeIfAbsent(key, k -> new TokenBucket(maxRequestsPerMinute, 60_000L));
        if (!bucket.tryConsume()) {
            log.warn("Rate limit exceeded for key: {}", key);
            throw new ApiException(ErrorCode.RATE_LIMITED, "Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    public void checkAuthRateLimit(String clientIp, String endpoint) {
        int limit = switch (endpoint) {
            case "/api/v1/auth/forgot-password" -> 5;
            case "/api/v1/auth/login" -> 10;
            case "/api/v1/auth/change-password" -> 10;
            case "/api/v1/auth/refresh" -> 15;
            default -> 60;
        };
        checkRateLimit(clientIp + ":" + endpoint, limit);
    }
}
