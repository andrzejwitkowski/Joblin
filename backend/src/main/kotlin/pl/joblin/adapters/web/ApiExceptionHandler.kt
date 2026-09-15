package pl.joblin.adapters.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pl.joblin.application.ForbiddenException
import pl.joblin.application.NotFoundException
import pl.joblin.application.UnauthorizedException

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    fun notFound(ex: NotFoundException) = ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message))

    @ExceptionHandler(ForbiddenException::class)
    fun forbidden(ex: ForbiddenException) = ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to ex.message))

    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorized(ex: UnauthorizedException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to ex.message))

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to ex.message))
}
