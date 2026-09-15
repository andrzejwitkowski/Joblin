package pl.joblin.application

import pl.joblin.domain.Clock
import pl.joblin.domain.JobOffer
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.OfferFilter
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role
import pl.joblin.domain.SourceBot
import pl.joblin.domain.User
import pl.joblin.domain.UserRepository
import java.time.Instant

class ListOffers(private val offers: JobOfferRepository) {
    fun execute(
        actor: User,
        ownerUserId: String?,
        status: OfferStatus?,
        sourceBot: SourceBot?,
        from: Instant?,
        to: Instant?,
    ): List<JobOffer> {
        val owner = when {
            actor.role == Role.ADMIN -> ownerUserId ?: throw ForbiddenException("ownerUserId required for admin")
            else -> actor.id
        }
        return offers.findByFilter(OfferFilter(owner, status, sourceBot, from, to))
    }
}

class GetOffer(private val offers: JobOfferRepository) {
    fun execute(actor: User, id: String): JobOffer = offers.requireAccessible(actor, id)
}

class UpdateOfferStatus(
    private val offers: JobOfferRepository,
    private val clock: Clock,
) {
    fun execute(actor: User, id: String, status: OfferStatus): JobOffer {
        val offer = offers.requireAccessible(actor, id)
        return offers.save(offer.copy(status = status, updatedAt = clock.now()))
    }
}

class ListUsers(private val users: UserRepository) {
    fun execute(actor: User): List<User> {
        if (actor.role != Role.ADMIN) throw ForbiddenException("Admin only")
        return users.findAll()
    }
}

private fun JobOfferRepository.requireAccessible(actor: User, id: String): JobOffer {
    val offer = findById(id) ?: throw NotFoundException("Offer not found")
    if (actor.role != Role.ADMIN && offer.ownerUserId != actor.id) {
        throw ForbiddenException("Not your offer")
    }
    return offer
}
