package com.platform.collector.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version // Import this!
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Represents an Issue/Incident in the Secondary DB.
 */
@Document(collection = "alerts")
data class Alert(
    @Id
    val id: String? = null,
    val logId: String,
    val serviceName: String,
    val endpoint: String,
    val issueType: String,
    val timestamp: Instant,
    val resolved: Boolean = false,
    val resolvedAt: Instant? = null,

    // --- CONCURRENCY REQUIREMENT (Page 4) ---
    @Version
    val version: Long? = null
)