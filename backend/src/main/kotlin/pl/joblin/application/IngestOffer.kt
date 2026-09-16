package pl.joblin.application

import jakarta.validation.constraints.NotBlank
import pl.joblin.domain.Clock
import pl.joblin.domain.IdProvider
import pl.joblin.domain.JobOffer
import pl.joblin.domain.JobOfferRepository
import pl.joblin.domain.OfferSection
import pl.joblin.domain.OfferSectionValidator
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
    val schemaVersion: Int? = null,
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

        val validated = OfferSectionValidator.validate(command.schemaVersion, command.sections)
        if (validated.errors.isNotEmpty()) throw OfferValidationException(validated.errors)

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
            schemaVersion = validated.schemaVersion,
            sections = command.sections,
        )
        val result = conflicts.execute { offers.upsertIngest(draft) }
        return IngestResult(result.offer.id, result.created)
    }
}
