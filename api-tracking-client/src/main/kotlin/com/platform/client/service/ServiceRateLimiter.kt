package com.platform.client.service

import com.google.common.util.concurrent.RateLimiter
import com.platform.client.config.MonitoringProperties
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class ServiceRateLimiter(
    private val properties: MonitoringProperties
) {
    // Guava RateLimiter acts as the token bucket
    private lateinit var rateLimiter: RateLimiter

    @PostConstruct
    fun init() {
        // Initialize based on application.yaml config (default 100.0)
        rateLimiter = RateLimiter.create(properties.rateLimit.limit)
    }

    /**
     * Checks if we can proceed.
     * Returns TRUE if we have permission (limit not hit).
     * Returns FALSE if we exceeded the limit (limit hit).
     */
    fun tryAcquire(): Boolean {
        if (!properties.rateLimit.enabled) return true
        return rateLimiter.tryAcquire()
    }
}