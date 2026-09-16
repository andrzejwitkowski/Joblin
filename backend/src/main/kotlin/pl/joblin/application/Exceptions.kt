package pl.joblin.application

import pl.joblin.domain.OfferFieldError

class NotFoundException(message: String) : RuntimeException(message)

class ForbiddenException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)

class OfferValidationException(
    val errors: List<OfferFieldError>,
) : RuntimeException(errors.joinToString("; ") { "${it.field}: ${it.message}" })
