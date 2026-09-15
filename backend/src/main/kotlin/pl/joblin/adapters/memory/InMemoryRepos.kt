package pl.joblin.adapters.memory

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import pl.joblin.domain.JobOffer
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.OfferFilter
import pl.joblin.domain.User
import pl.joblin.domain.UserRepository
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
            .filter { filter.status == null || it.status == filter.status }
            .filter { filter.sourceBot == null || it.sourceBot == filter.sourceBot }
            .filter { filter.from == null || !it.foundAt.isBefore(filter.from) }
            .filter { filter.to == null || !it.foundAt.isAfter(filter.to) }
            .sortedByDescending { it.foundAt }

    override fun save(offer: JobOffer): JobOffer {
        byId[offer.id] = offer
        return offer
    }

    fun clear() = byId.clear()
}
