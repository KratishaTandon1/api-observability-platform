package com.platform.collector.config

import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory

@Configuration
class DualMongoConfig {

    // --- PRIMARY DB (LOGS) ---
    // We name this 'mongoTemplate' so Spring Boot finds it automatically!
    
    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.primary")
    fun logsProperties(): MongoProperties = MongoProperties()

    @Primary
    @Bean
    fun logsFactory(logsProperties: MongoProperties): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(logsProperties.uri)
    }

    @Primary
    @Bean(name = ["mongoTemplate"]) // FIXED: Renamed from 'logsTemplate' to 'mongoTemplate'
    fun logsTemplate(logsFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(logsFactory)
    }

    // --- SECONDARY DB (METADATA) ---

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb.secondary")
    fun metadataProperties(): MongoProperties = MongoProperties()

    @Bean
    fun metadataFactory(metadataProperties: MongoProperties): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(metadataProperties.uri)
    }

    @Bean(name = ["metadataTemplate"])
    fun metadataTemplate(metadataFactory: MongoDatabaseFactory): MongoTemplate {
        return MongoTemplate(metadataFactory)
    }
}