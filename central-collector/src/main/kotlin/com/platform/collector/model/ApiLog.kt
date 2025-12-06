// package com.platform.collector.model

// import org.springframework.data.annotation.Id
// import org.springframework.data.mongodb.core.mapping.Document
// import java.time.Instant

// @Document(collection = "api_logs")
// data class ApiLog(
//     @Id
//     val id: String? = null,
//     val serviceName: String,
//     val endpoint: String,
//     val method: String,        // We will use simple "method"
//     val status: Int,           // We will use simple "status"
//     val duration: Long,        // We will use simple "duration"
//     val requestSize: Long,
//     val responseSize: Long,
//     val timestamp: Instant = Instant.now(),
//     val rateLimitHit: Boolean = false, // Simple "rateLimitHit"
//     val resolved: Boolean = false
// )



package com.platform.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "api_logs")
data class ApiLog(
    @Id
    val id: String? = null,
    val serviceName: String,
    val endpoint: String,
    val method: String,
    val status: Int,
    val duration: Long,
    val requestSize: Long,
    val responseSize: Long,
    val timestamp: Instant = Instant.now(),
    val rateLimitHit: Boolean = false,
    val resolved: Boolean = false
) {
    // Computed properties to fix "Unresolved reference" errors
    val isSlow: Boolean
        get() = duration > 500

    val isBroken: Boolean
        get() = status >= 500
}