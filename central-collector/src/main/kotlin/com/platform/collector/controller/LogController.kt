// package com.platform.collector.controller

// import com.platform.collector.model.ApiLog
// import com.platform.collector.service.LogIngestionService
// import org.springframework.http.ResponseEntity
// import org.springframework.web.bind.annotation.*

// @RestController
// @RequestMapping("/api/v1/logs")
// @CrossOrigin(origins = ["http://localhost:3000"]) // Allows Next.js to access this API
// class LogController(
//     private val service: LogIngestionService
// ) {

//     /**
//      * Ingests a batch of logs from the tracking client.
//      */
//     @PostMapping
//     fun ingestLogs(@RequestBody logs: List<ApiLog>): ResponseEntity<String> {
//         service.saveLogs(logs)
//         return ResponseEntity.accepted().body("Received ${logs.size} logs")
//     }

//     /**
//      * Returns all logs to the dashboard.
//      * In a production app, you would add pagination parameters here (e.g., ?page=0&size=50).
//      */
//     @GetMapping
//     fun getLogs(): ResponseEntity<List<ApiLog>> {
//         val logs = service.getAllLogs()
//         return ResponseEntity.ok(logs)
//     }
// }



package com.platform.collector.controller

import com.platform.collector.model.ApiLog
import com.platform.collector.service.LogIngestionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/logs")
@CrossOrigin(origins = ["http://localhost:3000"])
class LogController(
    private val service: LogIngestionService
) {

    @PostMapping
    fun ingestLogs(@RequestBody logs: List<ApiLog>): ResponseEntity<String> {
        service.saveLogs(logs)
        return ResponseEntity.accepted().body("Received ${logs.size} logs")
    }

    @GetMapping
    fun getLogs(): ResponseEntity<List<ApiLog>> {
        val logs = service.getAllLogs()
        return ResponseEntity.ok(logs)
    }

    @PutMapping("/{id}/resolve")
    fun resolveLog(@PathVariable id: String): ResponseEntity<String> {
        service.resolveIssue(id)
        return ResponseEntity.ok("Issue resolved")
    }
}