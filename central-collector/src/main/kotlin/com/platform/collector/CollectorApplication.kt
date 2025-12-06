package com.platform.collector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CollectorApplication

fun main(args: Array<String>) {
    runApplication<CollectorApplication>(*args)
}