package com.platform.collector.controller

import com.platform.collector.model.User
import com.platform.collector.util.JwtUtil
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val jwtUtil: JwtUtil
) {

    data class LoginRequest(val username: String, val password: String)
    data class LoginResponse(val token: String)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Any {
        // Hardcoded admin for assignment simplicity
        // In a real app, check DB: userRepository.findByUsername(request.username)
        if (request.username == "admin" && request.password == "admin123") {
            val token = jwtUtil.generateToken(request.username)
            return LoginResponse(token)
        }
        return mapOf("error" to "Invalid credentials")
    }
}