package pl.joblin.adapters.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.joblin.application.ForbiddenException
import pl.joblin.application.NotFoundException
import pl.joblin.application.UnauthorizedException

@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NotFoundException::class)
    fun notFound(ex: NotFoundException): ResponseEntity<Map<String, String>> {
        log.debug("Not found", ex)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Not found"))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun forbidden(ex: ForbiddenException): ResponseEntity<Map<String, String>> {
        log.debug("Forbidden", ex)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Forbidden"))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorized(ex: UnauthorizedException): ResponseEntity<Map<String, String>> {
        log.debug("Unauthorized", ex)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Unauthorized"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        log.debug("Bad request", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "Invalid request"))
    }
}
