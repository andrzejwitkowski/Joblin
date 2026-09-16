package pl.joblin.adapters.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.joblin.application.ForbiddenException
import pl.joblin.application.NotFoundException
import pl.joblin.application.OfferValidationException
import pl.joblin.application.UnauthorizedException

@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NotFoundException::class)
    fun notFound(ex: NotFoundException) = error(HttpStatus.NOT_FOUND, "Not found", ex)

    @ExceptionHandler(ForbiddenException::class)
    fun forbidden(ex: ForbiddenException) = error(HttpStatus.FORBIDDEN, "Forbidden", ex)

    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorized(ex: UnauthorizedException) = error(HttpStatus.UNAUTHORIZED, "Unauthorized", ex)

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException) = error(HttpStatus.BAD_REQUEST, "Invalid request", ex)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun unreadable(ex: HttpMessageNotReadableException) =
        error(HttpStatus.BAD_REQUEST, "Invalid request body", ex)

    @ExceptionHandler(OfferValidationException::class)
    fun offerValidation(ex: OfferValidationException): ResponseEntity<Map<String, Any>> {
        log.debug("Offer validation failed", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("errors" to ex.errors))
    }

    private fun error(status: HttpStatus, message: String, ex: Exception): ResponseEntity<Map<String, String>> {
        log.debug(message, ex)
        return ResponseEntity.status(status).body(mapOf("error" to message))
    }
}
