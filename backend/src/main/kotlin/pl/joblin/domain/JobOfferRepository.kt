package pl.joblin.domain

import java.time.Instant

data class OfferFilter(
    val ownerUserId: String,
    val status: OfferStatus? = null,
    val sourceBot: SourceBot? = null,
    val from: Instant? = null,
    val to: Instant? = null,
)

data class UpsertResult(
    val offer: JobOffer,
    val created: Boolean,
)

interface JobOfferRepository {
    fun findById(id: String): JobOffer?
    fun findByOwnerAndSourceUrl(ownerUserId: String, sourceUrl: String): JobOffer?
    fun findByFilter(filter: OfferFilter): List<JobOffer>
    /** Persist with optimistic version check; throws OptimisticLockingFailureException on conflict. */
    fun save(offer: JobOffer): JobOffer
    /** Insert or refresh ingest fields; never overwrites existing status. Retries on version conflicts. */
    fun upsertIngest(offer: JobOffer): UpsertResult
}
