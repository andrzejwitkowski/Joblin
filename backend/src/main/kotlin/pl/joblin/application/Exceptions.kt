package pl.joblin.application

class NotFoundException(message: String) : RuntimeException(message)

class ForbiddenException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)

data class OfferFieldError(
    val field: String,
    val code: String,
    val message: String,
)

class OfferValidationException(
    val errors: List<OfferFieldError>,
) : RuntimeException(errors.joinToString("; ") { "${it.field}: ${it.message}" })
