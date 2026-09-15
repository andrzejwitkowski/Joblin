package pl.joblin.domain

import java.time.Instant

data class OfferFilter(
    val ownerUserId: String,
    val status: OfferStatus? = null,
    val sourceBot: SourceBot? = null,
    val from: Instant? = null,
    val to: Instant? = null,
)

interface JobOfferRepository {
    fun findById(id: String): JobOffer?
    fun findByOwnerAndSourceUrl(ownerUserId: String, sourceUrl: String): JobOffer?
    fun findByFilter(filter: OfferFilter): List<JobOffer>
    fun save(offer: JobOffer): JobOffer
}
