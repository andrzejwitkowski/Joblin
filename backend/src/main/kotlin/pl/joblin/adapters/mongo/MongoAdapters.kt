package pl.joblin.adapters.mongo

import org.springframework.context.annotation.Profile
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import pl.joblin.domain.JobOffer
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.OfferFilter
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.Role
import pl.joblin.domain.SourceBot
import pl.joblin.domain.UpsertResult
import pl.joblin.domain.User
import pl.joblin.domain.UserRepository
import java.time.Instant

@Document("users")
data class UserDocument(
    @Id val id: String,
    @Indexed(unique = true) val email: String,
    val displayName: String,
    val role: Role,
    @Indexed(unique = true) val apiKeyId: String,
    val apiKeyHash: String,
    val createdAt: Instant,
)

@Document("offers")
@CompoundIndex(name = "owner_url", def = "{'ownerUserId': 1, 'sourceUrl': 1}", unique = true)
data class OfferDocument(
    @Id val id: String,
    val ownerUserId: String,
    val sourceUrl: String,
    val title: String,
    val company: String,
    val description: String,
    val salary: String?,
    val tags: List<String>,
    val sourceBot: SourceBot,
    val status: OfferStatus,
    val foundAt: Instant,
    val updatedAt: Instant,
    @Version val version: Long? = null,
)

@Repository
@Profile("!test")
class MongoUserRepository(
    private val mongo: MongoTemplate,
) : UserRepository {
    override fun findById(id: String) =
        mongo.findById(id, UserDocument::class.java)?.toDomain()

    override fun findByEmail(email: String) =
        mongo.findOne(Query.query(Criteria.where("email").`is`(email)), UserDocument::class.java)?.toDomain()

    override fun findByApiKeyId(apiKeyId: String) =
        mongo.findOne(Query.query(Criteria.where("apiKeyId").`is`(apiKeyId)), UserDocument::class.java)?.toDomain()

    override fun findAll() =
        mongo.findAll(UserDocument::class.java).map { it.toDomain() }

    override fun save(user: User): User {
        mongo.save(user.toDoc())
        return user
    }
}

@Repository
@Profile("!test")
class MongoJobOfferRepository(
    private val mongo: MongoTemplate,
) : JobOfferRepository {
    override fun findById(id: String): JobOffer? =
        mongo.findById(id, OfferDocument::class.java)?.toDomain()

    override fun findByOwnerAndSourceUrl(ownerUserId: String, sourceUrl: String): JobOffer? {
        val query = Query.query(
            Criteria.where("ownerUserId").`is`(ownerUserId)
                .and("sourceUrl").`is`(sourceUrl),
        )
        return mongo.findOne(query, OfferDocument::class.java)?.toDomain()
    }

    override fun findByFilter(filter: OfferFilter): List<JobOffer> {
        val criteria = mutableListOf(Criteria.where("ownerUserId").`is`(filter.ownerUserId))
        filter.status?.let { criteria += Criteria.where("status").`is`(it) }
        filter.sourceBot?.let { criteria += Criteria.where("sourceBot").`is`(it) }
        filter.from?.let { criteria += Criteria.where("foundAt").gte(it) }
        filter.to?.let { criteria += Criteria.where("foundAt").lte(it) }

        val query = Query(Criteria().andOperator(*criteria.toTypedArray()))
            .with(Sort.by(Sort.Direction.DESC, "foundAt"))
        return mongo.find(query, OfferDocument::class.java).map { it.toDomain() }
    }

    override fun save(offer: JobOffer): JobOffer {
        val saved = mongo.save(offer.toDoc())
        return saved.toDomain()
    }

    override fun upsertIngest(offer: JobOffer): UpsertResult {
        repeat(8) {
            val existing = findByOwnerAndSourceUrl(offer.ownerUserId, offer.sourceUrl)
            if (existing == null) {
                try {
                    return UpsertResult(save(offer), created = true)
                } catch (_: DuplicateKeyException) {
                    // concurrent insert — retry as update
                } catch (_: OptimisticLockingFailureException) {
                    // retry
                }
            } else {
                val refreshed = existing.copy(
                    title = offer.title,
                    company = offer.company,
                    description = offer.description,
                    salary = offer.salary,
                    tags = offer.tags,
                    sourceBot = offer.sourceBot,
                    foundAt = offer.foundAt,
                    updatedAt = offer.updatedAt,
                )
                try {
                    return UpsertResult(save(refreshed), created = false)
                } catch (_: OptimisticLockingFailureException) {
                    // retry
                }
            }
        }
        throw OptimisticLockingFailureException("upsertIngest exhausted retries")
    }
}

private fun UserDocument.toDomain() =
    User(id, email, displayName, role, apiKeyId, apiKeyHash, createdAt)

private fun User.toDoc() =
    UserDocument(id, email, displayName, role, apiKeyId, apiKeyHash, createdAt)

private fun OfferDocument.toDomain() =
    JobOffer(
        id, ownerUserId, sourceUrl, title, company, description, salary, tags,
        sourceBot, status, foundAt, updatedAt, version ?: 0,
    )

private fun JobOffer.toDoc() =
    OfferDocument(
        id, ownerUserId, sourceUrl, title, company, description, salary, tags,
        sourceBot, status, foundAt, updatedAt, version,
    )
