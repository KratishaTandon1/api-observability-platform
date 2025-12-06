package com.platform.collector.repository

import com.platform.collector.model.ApiLog
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

// Note: In a real dual-config setup, we often use MongoTemplate directly
// or specific @EnableMongoRepositories configs. 
// For simplicity, this interface assumes standard injection, 
// but we will use the Service layer to ensure the right template is used.

@Repository
interface ApiLogRepository : MongoRepository<ApiLog, String>