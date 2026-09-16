package pl.joblin.application

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import pl.joblin.domain.Clock
import pl.joblin.domain.IdProvider
import pl.joblin.domain.JobOffer
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.OfferLimits
import pl.joblin.domain.OfferSection
import pl.joblin.domain.OfferStatus
import pl.joblin.domain.SourceBot
import pl.joblin.domain.SourceUrlCanonicalizer
import pl.joblin.domain.UserRepository
import java.time.Instant

data class IngestOfferCommand(
    @field:NotBlank val userId: String,
    @field:NotBlank val sourceUrl: String,
    @field:NotBlank val title: String,
    @field:NotBlank val company: String,
    @field:NotBlank val description: String,
    val salary: String? = null,
    val tags: List<String> = emptyList(),
    val sourceBot: SourceBot,
    val foundAt: Instant? = null,
    val location: String? = null,
    val workMode: String? = null,
    val employmentLabel: String? = null,
    @field:Min(1)
    @field:Max(OfferLimits.MAX_SCHEMA_VERSION.toLong())
    val schemaVersion: Int? = null,
    @field:Size(max = OfferLimits.MAX_SECTIONS)
    val sections: List<OfferSection> = emptyList(),
)

data class IngestResult(
    val id: String,
    val created: Boolean,
)

class IngestOffer(
    private val users: UserRepository,
    private val offers: JobOfferRepository,
    private val clock: Clock,
    private val ids: IdProvider,
    private val conflicts: ConflictRetry,
) {
    fun execute(apiKeyUserId: String, command: IngestOfferCommand): IngestResult {
        if (apiKeyUserId != command.userId) {
            throw ForbiddenException("API key does not match userId")
        }
        users.findById(command.userId) ?: throw NotFoundException("User not found")

        val now = clock.now()
        val draft = JobOffer(
            id = ids.newId(),
            ownerUserId = command.userId,
            sourceUrl = SourceUrlCanonicalizer.canonicalize(command.sourceUrl),
            title = command.title,
            company = command.company,
            description = command.description,
            salary = command.salary,
            tags = command.tags,
            sourceBot = command.sourceBot,
            status = OfferStatus.NEW,
            foundAt = command.foundAt ?: now,
            updatedAt = now,
            version = 0,
            location = command.location,
            workMode = command.workMode,
            employmentLabel = command.employmentLabel,
            schemaVersion = command.schemaVersion ?: OfferLimits.MAX_SCHEMA_VERSION,
            sections = command.sections,
        )
        val result = conflicts.execute { offers.upsertIngest(draft) }
        return IngestResult(result.offer.id, result.created)
    }
}
