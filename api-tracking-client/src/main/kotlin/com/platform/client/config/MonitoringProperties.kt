package com.platform.client.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "monitoring")
data class MonitoringProperties(
    var enabled: Boolean = true,
    var serviceName: String = "unknown-service",
    var collectorUrl: String = "http://localhost:8080/api/v1/logs",
    var rateLimit: RateLimitProps = RateLimitProps()
) {
    data class RateLimitProps(
        var enabled: Boolean = true,
        var limit: Double = 100.0 // Default 100 requests per second
    )
}