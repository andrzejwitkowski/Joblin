package pl.joblin.domain

import java.time.Instant

data class JobOffer(
    val id: String,
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
    val version: Long? = null,
)
