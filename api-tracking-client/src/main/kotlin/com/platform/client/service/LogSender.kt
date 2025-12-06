package com.platform.client.service

import com.platform.client.config.MonitoringProperties
import com.platform.client.model.LogData
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture

@Service
class LogSender(
    private val properties: MonitoringProperties
) {
    private val restTemplate = RestTemplate()

    fun sendLogAsync(log: LogData) {
        // Send asynchronously so we don't slow down the main user request
        CompletableFuture.runAsync {
            try {
                restTemplate.postForLocation(properties.collectorUrl, listOf(log))
            } catch (e: Exception) {
                // In production, use a logger. We suppress errors so the main app doesn't crash.
                println("Failed to send log to collector: ${e.message}")
            }
        }
    }
}