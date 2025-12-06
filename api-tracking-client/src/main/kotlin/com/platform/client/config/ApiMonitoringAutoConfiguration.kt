package com.platform.client.config

import com.platform.client.interceptor.ApiTrackingInterceptor
import com.platform.client.service.LogSender
import com.platform.client.service.ServiceRateLimiter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(MonitoringProperties::class)
@ConditionalOnProperty(prefix = "monitoring", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Import(ServiceRateLimiter::class, LogSender::class, ApiTrackingInterceptor::class)
class ApiMonitoringAutoConfiguration(
    private val interceptor: ApiTrackingInterceptor
) : WebMvcConfigurer {

    @Bean
    fun monitoringProperties(): MonitoringProperties {
        return MonitoringProperties()
    }

    // Automatically register the interceptor in the application's request pipeline
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(interceptor)
            .addPathPatterns("/**") // Track ALL endpoints
            .excludePathPatterns("/actuator/**", "/error") // Ignore internal Spring endpoints
    }
}