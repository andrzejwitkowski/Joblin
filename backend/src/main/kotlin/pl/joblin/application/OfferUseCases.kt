package pl.joblin.application

import pl.joblin.domain.Clock
import pl.joblin.domain.JobOffer
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.OfferFade
import pl.joblin.domain.OfferFilter
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role
import pl.joblin.domain.SourceBot
import pl.joblin.domain.User
import pl.joblin.domain.UserRepository
import java.time.Instant

class SoftDeleteExpiredOffers(
    private val offers: JobOfferRepository,
    private val clock: Clock,
    private val conflicts: ConflictRetry,
) {
    fun execute(ownerUserId: String? = null): Int {
        val now = clock.now()
        val cutoff = now.minus(OfferFade.DURATION)
        var deleted = 0
        for (snapshot in offers.findTerminalNonDeleted(ownerUserId)) {
            val didDelete = conflicts.execute {
                val current = offers.findById(snapshot.id) ?: return@execute false
                if (current.isDeleted || !OfferFade.isTerminal(current.status)) return@execute false
                val fadeStart = current.fadeStartedAt ?: current.updatedAt
                if (fadeStart.isAfter(cutoff)) {
                    if (current.fadeStartedAt == null) {
                        offers.save(current.copy(fadeStartedAt = fadeStart))
                    }
                    return@execute false
                }
                offers.save(
                    current.copy(
                        fadeStartedAt = fadeStart,
                        isDeleted = true,
                        deletedAt = now,
                        updatedAt = now,
                    ),
                )
                true
            }
            if (didDelete) deleted++
        }
        return deleted
    }
}

class ListOffers(
    private val offers: JobOfferRepository,
    private val softDelete: SoftDeleteExpiredOffers,
) {
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
        softDelete.execute(owner)
        return offers.findByFilter(OfferFilter(owner, status, sourceBot, from, to))
    }
}

class GetOffer(private val offers: JobOfferRepository) {
    fun execute(actor: User, id: String): JobOffer = offers.requireAccessible(actor, id)
}

class UpdateOfferStatus(
    private val offers: JobOfferRepository,
    private val clock: Clock,
    private val conflicts: ConflictRetry,
) {
    fun execute(actor: User, id: String, status: OfferStatus): JobOffer =
        conflicts.execute {
            val offer = offers.requireAccessible(actor, id)
            val now = clock.now()
            val fadeStartedAt = when {
                !OfferFade.isTerminal(status) -> null
                OfferFade.isTerminal(offer.status) -> offer.fadeStartedAt ?: now
                else -> now
            }
            offers.save(
                offer.copy(
                    status = status,
                    updatedAt = now,
                    fadeStartedAt = fadeStartedAt,
                ),
            )
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
    if (offer.isDeleted) throw NotFoundException("Offer not found")
    if (actor.role != Role.ADMIN && offer.ownerUserId != actor.id) {
        throw ForbiddenException("Not your offer")
    }
    return offer
}
