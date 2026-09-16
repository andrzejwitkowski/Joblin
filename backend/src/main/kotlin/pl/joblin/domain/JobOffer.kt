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
    val version: Long = 0,
    val location: String? = null,
    val workMode: String? = null,
    val employmentLabel: String? = null,
    val schemaVersion: Int = OfferLimits.MAX_SCHEMA_VERSION,
    val sections: List<OfferSection> = emptyList(),
)

fun JobOffer.withIngestedContent(incoming: JobOffer): JobOffer = copy(
    title = incoming.title,
    company = incoming.company,
    description = incoming.description,
    salary = incoming.salary,
    tags = incoming.tags,
    sourceBot = incoming.sourceBot,
    foundAt = incoming.foundAt,
    updatedAt = incoming.updatedAt,
    location = incoming.location,
    workMode = incoming.workMode,
    employmentLabel = incoming.employmentLabel,
    schemaVersion = incoming.schemaVersion,
    sections = incoming.sections,
)
