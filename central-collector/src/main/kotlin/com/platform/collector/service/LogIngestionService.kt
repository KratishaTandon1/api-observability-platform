// package com.platform.collector.service

// import com.platform.collector.model.ApiLog
// import com.platform.collector.repository.ApiLogRepository
// import org.springframework.stereotype.Service

// @Service
// class LogIngestionService(
//     private val repository: ApiLogRepository
// ) {

//     /**
//      * Saves a batch of logs to the Primary MongoDB (Logs DB).
//      */
//     fun saveLogs(logs: List<ApiLog>) {
//         if (logs.isNotEmpty()) {
//             repository.saveAll(logs)
//         }
//     }

//     /**
//      * Retrieves all logs from the database for the dashboard.
//      */
//     fun getAllLogs(): List<ApiLog> {
//         return repository.findAll()
//     }
// }



package com.platform.collector.service

import com.platform.collector.model.ApiLog
import com.platform.collector.model.Alert
import com.platform.collector.repository.ApiLogRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class LogIngestionService(
    private val repository: ApiLogRepository,
    @Qualifier("metadataTemplate") private val metadataTemplate: MongoTemplate
) {

    fun saveLogs(logs: List<ApiLog>) {
        if (logs.isEmpty()) return

        // 1. Save to Primary DB
        val savedLogs = repository.saveAll(logs)

        // 2. Check for issues and save to Secondary DB
        val alerts = savedLogs.mapNotNull { log ->
            var issueType: String? = null
            
            // Checks computed properties in ApiLog
            if (log.isBroken) issueType = "BROKEN"
            else if (log.isSlow) issueType = "SLOW"
            else if (log.rateLimitHit) issueType = "RATE_LIMIT"

            if (issueType != null) {
                Alert(
                    logId = log.id!!,
                    serviceName = log.serviceName,
                    endpoint = log.endpoint,
                    issueType = issueType,
                    timestamp = log.timestamp
                )
            } else null
        }

        if (alerts.isNotEmpty()) {
            metadataTemplate.insertAll(alerts)
        }
    }

    fun getAllLogs(): List<ApiLog> {
        val logs = repository.findAll()
        val alerts = metadataTemplate.findAll(Alert::class.java)
        
        // Map LogID -> Resolved Status
        val resolvedMap = alerts.associate { it.logId to it.resolved }

        return logs.map { log ->
            val isResolved = resolvedMap[log.id] ?: false
            log.copy(resolved = isResolved)
        }
    }

    /**
     * ROBUST RESOLVE: Handles both new and old data safely.
     */
    fun resolveIssue(logId: String) {
        // FIXED: Backticks around `is` to prevent Kotlin syntax error
        val query = Query(Criteria.where("logId").`is`(logId))
        val update = Update().set("resolved", true).set("resolvedAt", Instant.now())
        
        // 1. Try to update existing Alert
        val result = metadataTemplate.updateFirst(query, update, Alert::class.java)

        // 2. If no Alert found (Old Data?), create a new Resolved Alert manually!
        if (result.modifiedCount == 0L) {
             val log = repository.findById(logId).orElse(null)
             if (log != null) {
                 val newAlert = Alert(
                     logId = log.id!!,
                     serviceName = log.serviceName,
                     endpoint = log.endpoint,
                     issueType = "MANUAL_RESOLVE", 
                     timestamp = log.timestamp,
                     resolved = true,
                     resolvedAt = Instant.now()
                 )
                 metadataTemplate.save(newAlert)
             }
        }
    }
}