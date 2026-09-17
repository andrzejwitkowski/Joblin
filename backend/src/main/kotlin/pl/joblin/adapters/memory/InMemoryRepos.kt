package pl.joblin.adapters.memory

import org.springframework.context.annotation.Profile
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository
import pl.joblin.domain.JobOffer
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.OfferFade
import pl.joblin.domain.OfferFilter
import pl.joblin.domain.UpsertResult
import pl.joblin.domain.User
import pl.joblin.domain.UserRepository
import pl.joblin.domain.withIngestedContent
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("test")
class InMemoryUserRepository : UserRepository {
    private val byId = ConcurrentHashMap<String, User>()

    override fun findById(id: String) = byId[id]
    override fun findByEmail(email: String) =
        byId.values.firstOrNull { it.email.equals(email, ignoreCase = true) }
    override fun findByApiKeyId(apiKeyId: String) =
        byId.values.firstOrNull { it.apiKeyId == apiKeyId }
    override fun findAll() = byId.values.toList()
    override fun save(user: User): User {
        byId[user.id] = user
        return user
    }

    fun clear() = byId.clear()
}

@Repository
@Profile("test")
class InMemoryJobOfferRepository : JobOfferRepository {
    private val byId = ConcurrentHashMap<String, JobOffer>()

    override fun findById(id: String) = byId[id]
    override fun findByOwnerAndSourceUrl(ownerUserId: String, sourceUrl: String) =
        byId.values.firstOrNull { it.ownerUserId == ownerUserId && it.sourceUrl == sourceUrl }

    override fun findByFilter(filter: OfferFilter): List<JobOffer> =
        byId.values
            .filter { it.ownerUserId == filter.ownerUserId }
            .filter { filter.includeDeleted || !it.isDeleted }
            .filter { filter.status == null || it.status == filter.status }
            .filter { filter.sourceBot == null || it.sourceBot == filter.sourceBot }
            .filter { filter.from == null || !it.foundAt.isBefore(filter.from) }
            .filter { filter.to == null || !it.foundAt.isAfter(filter.to) }
            .sortedByDescending { it.foundAt }

    override fun findTerminalNonDeleted(ownerUserId: String?): List<JobOffer> =
        byId.values
            .filter { !it.isDeleted && OfferFade.isTerminal(it.status) }
            .filter { ownerUserId == null || it.ownerUserId == ownerUserId }
            .toList()

    override fun save(offer: JobOffer): JobOffer {
        val existing = byId[offer.id]
        if (existing == null) {
            val clash = findByOwnerAndSourceUrl(offer.ownerUserId, offer.sourceUrl)
            if (clash != null) {
                throw DuplicateKeyException("owner_url already exists")
            }
            val inserted = offer.copy(version = 0)
            if (byId.putIfAbsent(offer.id, inserted) != null) {
                throw OptimisticLockingFailureException("concurrent insert on ${offer.id}")
            }
            return inserted
        }
        if (existing.version != offer.version) {
            throw OptimisticLockingFailureException("version mismatch for ${offer.id}")
        }
        val updated = offer.copy(version = existing.version + 1)
        if (!byId.replace(offer.id, existing, updated)) {
            throw OptimisticLockingFailureException("lost update for ${offer.id}")
        }
        return updated
    }

    override fun upsertIngest(offer: JobOffer): UpsertResult {
        val existing = findByOwnerAndSourceUrl(offer.ownerUserId, offer.sourceUrl)
        return if (existing == null) {
            UpsertResult(save(offer), created = true)
        } else {
            UpsertResult(save(existing.withIngestedContent(offer)), created = false)
        }
    }

    fun clear() = byId.clear()
}
