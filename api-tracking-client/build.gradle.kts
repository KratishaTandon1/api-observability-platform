plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

group = "com.platform"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Web (Required for Interceptors and RestTemplate/WebClient)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Configuration Processor (Reads application.yaml)
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    // Guava (REQUIRED for the Rate Limiter)
    implementation("com.google.guava:guava:33.0.0-jre")

    // Kotlin support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

// Disable building an executable jar (since this is a library)
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}