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
    fun save(offer: JobOffer): JobOffer
    /** Insert or refresh ingest fields; never overwrites existing status. */
    fun upsertIngest(offer: JobOffer): UpsertResult
    /** Atomically set status; returns null if missing. */
    fun updateStatus(id: String, status: OfferStatus, updatedAt: Instant): JobOffer?
}
