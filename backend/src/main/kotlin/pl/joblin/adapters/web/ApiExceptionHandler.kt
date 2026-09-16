package pl.joblin.adapters.web

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
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
import pl.joblin.domain.OfferFieldError

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

    @ExceptionHandler(OfferValidationException::class)
    fun offerValidation(ex: OfferValidationException): ResponseEntity<Map<String, Any>> {
        log.debug("Offer validation failed", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("errors" to ex.errors))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun unreadable(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, Any>> {
        log.debug("Unreadable request", ex)
        val cause = ex.cause
        val error = when (cause) {
            is InvalidTypeIdException -> OfferFieldError(
                field = jsonPath(cause),
                code = "UNKNOWN_SECTION_TYPE",
                message = "unknown section type '${cause.typeId}'",
            )
            is InvalidFormatException -> {
                if (cause.targetType?.isEnum == true) {
                    OfferFieldError(
                        field = jsonPath(cause),
                        code = "UNKNOWN_ENUM",
                        message = "value '${cause.value}' is not a valid ${cause.targetType.simpleName}",
                    )
                } else {
                    null
                }
            }
            else -> null
        }
        return if (error != null) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("errors" to listOf(error)))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "Invalid request body"))
        }
    }

    private fun jsonPath(ex: JsonMappingException): String =
        ex.path.joinToString(".") { it.fieldName ?: "[${it.index}]" }.ifBlank { "body" }
}
