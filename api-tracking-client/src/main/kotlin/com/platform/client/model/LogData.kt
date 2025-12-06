package com.platform.client.model

import java.time.Instant

data class LogData(
    val serviceName: String,
    val endpoint: String,
    val method: String,
    val status: Int,
    val duration: Long,
    val requestSize: Long,
    val responseSize: Long,
    val timestamp: Instant = Instant.now(),
    val rateLimitHit: Boolean = false
)