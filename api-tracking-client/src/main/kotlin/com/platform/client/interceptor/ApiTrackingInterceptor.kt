package com.platform.client.interceptor

import com.platform.client.config.MonitoringProperties
import com.platform.client.model.LogData
import com.platform.client.service.LogSender
import com.platform.client.service.ServiceRateLimiter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Instant

@Component
class ApiTrackingInterceptor(
    private val properties: MonitoringProperties,
    private val rateLimiter: ServiceRateLimiter,
    private val logSender: LogSender
) : HandlerInterceptor {

    // 1. Before the request is handled
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // Record start time
        request.setAttribute("startTime", System.currentTimeMillis())
        
        // Check Rate Limit
        val allowed = rateLimiter.tryAcquire()
        if (!allowed) {
            // Requirement: "The request should still continue normally"
            // But we mark it to log the violation
            request.setAttribute("rateLimitHit", true)
        }
        
        return true // Continue processing request
    }

    // 2. After the request is finished
    override fun afterCompletion(
        request: HttpServletRequest, 
        response: HttpServletResponse, 
        handler: Any, 
        ex: Exception?
    ) {
        val startTime = request.getAttribute("startTime") as Long
        val duration = System.currentTimeMillis() - startTime
        
        // Build the Log Object
        val logData = LogData(
            serviceName = properties.serviceName,
            endpoint = request.requestURI,
            method = request.method,
            status = response.status,
            duration = duration,
            requestSize = request.contentLengthLong, // Approx size
            responseSize = 0, // Hard to get in standard interceptor without wrappers, keeping 0 for now
            timestamp = Instant.now(),
            rateLimitHit = request.getAttribute("rateLimitHit") == true
        )

        // Send it to the collector
        logSender.sendLogAsync(logData)
    }
}